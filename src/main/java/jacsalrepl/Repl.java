/*
 * Copyright 2022 James Crawford
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package jacsalrepl;

import jacsal.JacsalContext;
import jacsal.Compiler;
import jacsal.EOFError;
import jacsal.JacsalError;
import org.jline.builtins.Completers;
import org.jline.reader.EndOfFileException;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.UserInterruptException;
import org.jline.reader.impl.completer.ArgumentCompleter;
import org.jline.reader.impl.completer.NullCompleter;
import org.jline.reader.impl.completer.StringsCompleter;
import org.jline.reader.impl.completer.SystemCompleter;
import org.jline.reader.impl.history.DefaultHistory;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import jacsal.runtime.RuntimeUtils;

import java.io.File;
import java.io.IOException;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.jline.builtins.Completers.TreeCompleter.node;

public class Repl {

  final static String helpText =
    "  :h       Help - print this text\n" +
    "  :?       Alias for :h\n" +
    "  :x       Exit\n" +
    "  :q       Quit - alias for :x\n" +
    "  :c       Clear current buffer\n" +
    "  :r file  Read and execute contents of file\n" +
    "  :l       Load - alias for :r\n" +
    "  :s       Show variables and their values (concise form)\n" +
    "  :S       Show variables and their values in pretty printed form\n" +
    "  :p       Purge variables\n" +
    "  :H [n]   Show recent history (last n entries - defaults to 50)\n" +
    "  :! n     Recall history entry with given number\n";

  final static String commands = "h?xqcrlsSpH!";

  public static void main(String[] args) throws Exception {
    Terminal terminal = TerminalBuilder.builder()
                                       .system(true)
                                       .build();
    DefaultHistory history = new DefaultHistory();
    var completer = new SystemCompleter();
    completer.add(":r", new Completers.FileNameCompleter());
    commands.chars().forEach(c -> completer.add(":" + (char)c, new NullCompleter()));
    completer.add("", new NullCompleter());
    completer.compile();
    LineReader reader = LineReaderBuilder.builder()
                                         .terminal(terminal)
                                         .variable(LineReader.HISTORY_FILE, System.getProperty("user.home") + "/.jacsal_history")
                                         .variable(LineReader.HISTORY_FILE_SIZE, 10000)
                                         .variable(LineReader.HISTORY_SIZE, 10000)
                                         .option(LineReader.Option.DISABLE_EVENT_EXPANSION, true)
                                         .history(history)
                                         .completer(completer)
                                         .build();

    JacsalContext      context = JacsalContext.create().replMode(true).build();
    Map<String,Object> globals = new HashMap<>();
    String             buffer  = null;
    final String primaryPrompt = "> ";
    String prompt = primaryPrompt;
    while (true) {
      boolean fileInput = false;
      try {
        fileInput          = false;
        String line        = reader.readLine(prompt);
        String trimmedLine = line.trim();
        if (trimmedLine.isEmpty()) { continue; }

        // Check for REPL command
        if (trimmedLine.startsWith(":")) {
          switch(trimmedLine.charAt(1)) {
            case 'q':  /* alias for x */
            case 'x':  System.exit(0);
            case 'c':  prompt = primaryPrompt; buffer = null; continue;
            case '?':  /* alias for h */
            case 'h':  System.out.println(helpText);          continue;
          }
          if (prompt.equals(primaryPrompt)) {
            String arg = trimmedLine.replaceAll("^:.\\s*","");
            switch (trimmedLine.charAt(1)) {
              case 's': globals.forEach((key,value) -> System.out.println(key + "=" + RuntimeUtils.toString(value)));           continue;
              case 'S': globals.forEach((key,value) -> System.out.println(key + "=" + RuntimeUtils.toString(value, 2))); continue;
              case 'p': globals.clear();   continue;
              case 'l': /* alias for r */
              case 'r': line = Files.readString(Path.of(arg));  fileInput = true;  break;
              case 'H':
                int count = arg.isEmpty() ? 50 : Integer.parseInt(arg);
                int last = history.last();
                for (int i = 0; i < count; i++) {
                  System.out.println((last-count+i) + ": " + history.get(last-count+i));
                }
                continue;
              case '!': {
                int entry = Integer.parseInt(arg);
                line = history.get(entry);
                System.out.println(line);
                history.add(line);
                break;
              }
            }
          }
        }

        prompt = "  ";
        buffer = buffer == null ? line : buffer + "\n" + line;
        Object result = Compiler.run(buffer, context, globals);
        if (result != null) {
          System.out.println(RuntimeUtils.toString(result));
        }
        buffer = null;  // Successfully executed so start with empty buffer
        prompt = primaryPrompt;
      }
      catch (EOFError e) {
        // Keep buffer and add lines to it if we get EOF error during compile from command line.
        // Otherwise treat as normal error
        if (fileInput) {
          System.out.println(e.getMessage());
          buffer = null;
          prompt = primaryPrompt;
        }
      }
      catch (JacsalError e) {
        System.out.println(e.getMessage());
        buffer = null;
        prompt = primaryPrompt;
      }
      catch (IOException e) {
        System.out.println("Error accessing file: " + e.getMessage());
      }
      catch (UserInterruptException e) {
        e.printStackTrace();
      }
      catch (EndOfFileException e) {
        System.exit(0);
      }
      catch (Exception e) {
        System.out.println(e.getMessage());
      }
    }
  }
}
