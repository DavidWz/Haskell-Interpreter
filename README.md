# Haskell Interpreter

This is a haskell interpreter written in Java. It's very slow and inefficient, so don't seriously use it (yet).

## Haskell Subset
Refer to `src/haskell/complex/parser/ComplexHaskell.g4` for the grammar of the supported subset of Haskell.  

As an example, consider the following data structure for lists in full haskell:  
`data List a = Nil | Cons (List a) a`  
A function which calculates the length of such a list can be defined as follows:  
`len :: (List a) -> Int`  
`len Nil = 0`  
`len (Cons xs _) = 1 + (len xs)`  
  
An equivalent supported function declaration looks like this:  
`len Nil = 0`  
`len (Cons xs _) = (plus 1 (len xs))`  

Refer to `test/haskell/SampleProgram.hs` for more examples.  

## Interactive Environment
Run the HaskellIntepreter jar to start the interactive environment. This program is similar to ghci.  
Type `:quit` to exit the program. Type `:load <filename>` to load a program.  
Simply type a function or pattern declaration to add it to the current program.  
Simply type an expression to evaluate it (don't forget the parenthesis! e.g. `(plus 1 2)`).  
You can use the ":verbose" command if you want to see all reduction steps (Warning: this might result in a lot of output).  