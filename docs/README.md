# Jacsal-Repl

The Jacsal-Repl project is a command line shell for [Jacsal](https://github.com/jaccomoc/jacsal) scripting
language.
It provides a Read-Evaluate-Print-Loop execution shell for testing simple Jacsal scripts, using the excellent
[JLine](https://github.com/jline/jline3) for command line editing and history.

# Building

## Requirements

* Java 11+
* JLine 3.21.0
* Jacsal 1.0
* Gradle 8.0.2

## Build

```shell
git clone https://github.com/jaccomoc/jacsal-repl.git
cd jacsal-repl
./gradlew build
```

That will build `jacsal-repl-1.0.jar` under the `build/libs` directory.

# Running

To run the REPL:
```shell
java -jar jacsal-repl-1.0.jar
```

This will present a single `> ` prompt where you can enter Jacsal code that will be compiled, executed, and the
result printed out.
If the code looks like it continues on a subsequent line, the REPL will output two spaces and wait for the additional
code to be entered.

For example:
```groovy
$ java -jar jacsal-repl-1.0.jar
> def isPrime(n) { n > 1 && !n.sqrt().filter{ it > 0 && n % (it+1) == 0 } }
Function@1864869682
> def primes = 100.map{ it + 1 }.filter(isPrime)
[2, 3, 5, 7, 11, 13, 17, 19, 23, 29, 31, 37, 41, 43, 47, 53, 59, 61, 67, 71, 73, 79, 83, 89, 97]
> primes.map{
    def fib(n) { n <= 2 ? 1 : fib(n - 1) + fib(n - 2) }
    fib(it)
  }.limit(10)
[1, 2, 5, 13, 89, 233, 1597, 4181, 28657, 514229]
>
```

You can use the up and down arrows on the keyboard to cycle through previous commands and the left and right arrows
to navigate within a recent command if you want to change anything.

Other keybindings map to common keybindings used by shells such as `bash` or `zsh` when in `emacs` mode.

The main ones to remember:

| Key Binding   | Description                                                                                                                                                                                                                                            |
|:--------------|:-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `<ctrl>-A`    | Beginning of line                                                                                                                                                                                                                                      |
 | `<ctrl>-E`    | End of line                                                                                                                                                                                                                                            |
| `<ctrl>-R`    | Search backwards for previous matching input line (incremental search based on characters entered after the `<ctrl>-R`).<br/>Once searching has started `<ctrl>-R` will continue searching for the previous matching entry to the one being displayed. |
 | `<ctrl>-L`    | Clear the screen                                                                                                                                                                                                                                       | 
 | `<ctrl>-U`    | Discard characters currently typed on the line                                                                                                                                                                                                         |

## Commands

As well as entering Jacsal code at the prompt, the REPL understands a limit set of commands that all start with `:`.
For example, `:h` or `:?` will print out the help text listing the commands available:
```
$ java -jar build/libs/jacsal-repl-1.0-SNAPSHOT.jar
> :h

Available commands:
  :h       Help - print this text
  :?       Alias for :h
  :x       Exit
  :q       Quit - alias for :x
  :c       Clear current buffer
  :r file  Read and execute contents of file
  :l       Load - alias for :r
  :s       Show variables and their values (concise form)
  :S       Show variables and their values in pretty printed form
  :p       Purge variables
  :H [n]   Show recent history (last n entries - defaults to 50)
  :! n     Recall history entry with given number

>
```

These are the commands:

| Command                  | Description                                                                                               |
|:-------------------------|:----------------------------------------------------------------------------------------------------------|
| `:h` `:?`                | Print help text.                                                                                          |
| `:x` `:q`                | Exit.                                                                                                     |
| `:c`                     | In the middle of a multi-line statement this will clear the buffer and allow you to start again.          |
| `:r file`<br/> `:l file` | Read/load a Jacsal script file and compile and run it. Tab completion is availabe for the file name.      |
| `:s`                     | Show all top level variables and their values.                                                            |
 | `:S`                     | Show all top level variables and pretty print their values                                                |
 | `:p`                     | Purge (delete) all variables.                                                                             |
 | `:H n`                   | Show recent history (default 50 entries). If a value for n is given then that many entries will be shown. |
 | `:! n`                   | Recall history entry `n` and evaluate it.                                                                 |

