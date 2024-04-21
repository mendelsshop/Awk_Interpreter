function a(x ,y)
{
    z[0] = x
    z[1] = y
    p[0] = (x * x)
    p[1] = (y * y)
    return x
}

{
     k = a(10, 1)
     print p[0]
    for (i in p) {
        print i
    }
}