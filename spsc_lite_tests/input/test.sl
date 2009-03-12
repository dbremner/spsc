append(Nil(), vs) = vs;
append(Cons(u, us), vs) = Cons(u, append(us, vs));

reverse(Nil()) = Nil();
reverse(Cons(x, xs)) = append(reverse(xs), Cons(x, Nil()));

from(n) = Cons(n, from(S(n)));

take(Z(), xs) = Nil();
take(S(n), xs) = Cons(head(xs), take(n, tail(xs)));

mapAdd1(Nil()) = Nil();
mapAdd1(Cons(x, xs)) = Cons(S(x), mapAdd1(xs));

null(Nil()) = True();
null(Cons(x, xs)) = False();

head(Cons(x, xs)) = x;

tail(Cons(x, xs)) = xs;

ab(A(x)) = B(ab(x));
ab(B(x)) = A(ab(x));

reva(Nil(), ys) = ys;
reva(Cons(x, xs), ys) = reva(xs, Cons(x, ys));

eq(Z(), y) = eqZ(y);
eq(S(x), y) = eqS(y, x);

eqZ(Z()) = True();
eqZ(S(x)) = False();

eqS(Z(), x) = False();
eqS(S(y), x) = eq(x, y);

if(True(), x, y) = x;
if(False(), x, y) = y;

not(x) = if(x, False(), True());
or(x, y) = if(x, True(), y);
and(x, y) = if(x, y, False());

member(x, list) = and(not(null(list)), or(eq(x, head(list)), member(x, tail(list))));
member2(Nil(), x) = False();
member2(Cons(y, ys), x) = if(eq(x, y), True(), member2(ys, x));
member3(Nil(), x) = False();
member3(Cons(y, ys), x) = member3if(eq(x, y), x, ys);
member3if(True(), x, ys) = True();
member3if(False(), x, ys) = member3(ys, x);

test1() = append(Nil(), Nil());
test2() = append(Cons(A1(), Cons(A2(), Nil())), Cons(A3(), Cons(A4(), Nil())));
test3() = reverse(Cons(A1(), Cons(A2(), Cons(A3(), Nil()))));
test4() = mapAdd1(from(Z()));
test5(x) = reverse(x);
test6(x) = ab(ab(x));
test7(xs) = reva(xs, Nil());
test8(x) = eq(x, x);
test9(x) = eq(x, S(x));
test10(x) = eq(S(x), x);
test11(x) = eq(S(Z()), x);
test12(x, y) = not(or(not(x), not(y)));
test13(x, list) = member(x, list);
test14(list) = member(S(Z()), list);
test15(x) = member(x, Cons(Z(), Cons(S(Z()), Nil())));
test16(x, list) = member2(list, x);
test17(list) = member2(list, S(Z()));
test18(x) = member2(Cons(Z(), Cons(S(Z()), Nil())), x);
test19(x, list) = member3(list, x);
test20(list) = member3(list, S(Z()));
test21(x) = member3(Cons(Z(), Cons(S(Z()), Nil())), x);
