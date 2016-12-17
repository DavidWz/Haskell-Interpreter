# Haskell Interpreter

This is a haskell interpreter written in Java.  
The underlying theory is taken from the functional programming course at the RWTH Aachen university. (http://verify.rwth-aachen.de/fp16/)  
  
## Haskell Subset
Refer to `src/haskell/complex/parser/ComplexHaskell.g4` for the grammar of the supported subset of Haskell.  
For a list of predefined functions, refer to `src/lambda/reduction/delta/PredefinedFunction.java`.  
  
As an example, consider the following data structure for lists in full haskell:  
`data List a = Nil | Cons a (List a)`  
A function which calculates the length of such a list can be defined as follows:  
`len :: (List a) -> Int`  
`len Nil = 0`  
`len (Cons _ xs) = 1 + (len xs)`  
  
An equivalent supported function declaration looks like this:  
`data List a = Nil | Cons a (List a)`  
`len Nil = 0`  
`len (Cons _ xs) = (plus 1 (len xs))`  
  
Refer to `test/haskell/SampleProgram.hs` for more examples.  
  
## Interactive Environment
Run the HaskellIntepreter jar to start the interactive environment. This program is similar to ghci.  
Type `:quit` to exit the program. Type `:load <filename>` to load a program.  
Simply type a function or pattern declaration to add it to the current program.  
Simply type an expression to evaluate it (don't forget the parenthesis! e.g. `(plus 1 2)`).  
You can use the `:verbose` command if you want to see all reduction steps (Warning: this might result in a lot of output!).  