elem(X, cons(X, _)).
elem(X, cons(_, T)) :- elem(X, T).
