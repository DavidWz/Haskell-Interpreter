len Nil = 0
len (Cons xs x) = (plus 1 (len xs))

genList 0 = Nil
genList x = (Cons (genList (decrement x)) x)

decrement x = (minus x 1)
