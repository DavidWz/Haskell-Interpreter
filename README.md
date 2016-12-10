# Haskell Interpreter

This is a haskell interpreter written in Java. It's very slow and inefficient, so don't seriously use it (yet).

## Haskell Subset

As for basic types, only integers and booleans are supported so far.  
No custom data structures can be defined. However, data structures can be used without defining them.  
No type declarations can be made.  
No infix operators or non-alphanumeric operators (such as +, -, >) are supported. You have to use the equivalent predefined  
functions: plus, minus, times, divided, pow, less, greater, lesseq, greatereq, equal, inequal, and, or, not.  
No `where` terms can be used. Please use `let` terms instead.  
No conditional function declarations can be used. Please use `if-then-else` instead.  
Function applications must be inside parenthesis.  
I probably missed quite a lot of things which are not supported, but you'll see when you try to use them.  
  
Consider the following data structure for lists in full haskell:  
`data List a = Nil | Cons (List a) a`  
A function which calculates the length of such a list can be defined as follows:  
`len :: (List a) -> Int`  
`len Nil = 0`  
`len (Cons xs _) = 1 + (len xs)`  
  
An equivalent supported function declaration looks like this:  
`len Nil = 0`  
`len (Cons xs _) = (plus 1 (len xs))`  

## Interactive Environment
Run the HaskellInteractiveEnvironment jar to start the interactive environment. This program is similar to ghci.  
Type `:quit` to exit the program. Type `:load <filename>` to load a program.  
Simply type a function or pattern declaration to add it to the current program.  
Simply type an expression to evaluate it.  