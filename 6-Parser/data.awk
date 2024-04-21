function dos(a,
b) {
	return a;
}
BEGIN {
   # print "Welcome to JDoodle!"
   z[0] = 6
   z[1] = 6
   # z[0][1] = dos
   a = 5
   b= -7
   b-=5 + (z[1] += 55)
   asdf = 0?  a+=7 : b = 6
   # means bacicly any operation can do mutation/assignent
   v = a ? v ~ (p = `4`) : 0

	dos(2, 4)
   # for (;;) {}
   # while () {}
#    for (6; 86 ;9) {print 6}
#    do  {++x} while (x)
}

{y+=1;}

y {
    while(!x)   # Comment
        if (++t) { y--;}
        else { --x;}
    --y;
}

NR!=1 {
    b(z?d:e);

    if (c=c=x&&y||!z) { 
        for(x-=a[a[1]||1]||(z&&0.0); c; --c) {
            z = 1; # Comment
        }
        #`0`
    }
}

BEGIN {
    x = 1;
    y = 2;
    z = 0;
}

{
    y += 1;
    if (NR % 2 == 0) {
        while (x) {
            if (--y) {
                for (i = 0; i < 5; i++) {
                    z += i;
                }
            } else {
                do {
                    z = z ? z : 0; # Nested comment
                } while (x && y);
            }
        }
    }
}

END {
    a = 1;
    b = 2;
    if (c = a && b || !z) {
        for (x -= a[a[1] || 1] || (z && 0.0); c; --c) {
            z = 1; # Set z to 1
        }
        #`0` - Comment
    }
}

BEGIN {
    x = 0;
    y = 1;
}

{
    y += 1;
    if (NR % 3 == 0) {
        do {
            x = x ? x : 1; # Comment
        } while (y && x);
    } else {
        for (i = 1; i <= 5; i++) {
            z *= i;
        }
    }
}

END {
    if (y > 10) {
        b = 3;
        c = 2;
        if (c == 2 && b != 4) {
            while (x) {
                for (j = 1; j <= 3; j++) {
                    z += j;
                }
            }
        }
        #`0` - Comment
    } else {
        d = 5;
        e = 6;
        if (d || e) {
            for (x -= 4; x; x++) {
                z = 1; # Set z to 1
            }
        }
    }
}
{a=1;b=2;c=3;d=4;e=5;f=6;g=7;h=8;i=9;j=10;k=11;l=12;m=13;n=14;o=15;p=16;q=17;r=18;s=19;t=20;u=21;v=22;w=23;x=24;y=25;z=26}
END{if(a==1){if(b==2){if(c==3){if(d==4){if(e==5){if(f==6){if(g==7){if(h==8){if(i==9){if(j==10){if(k==11){if(l==12){if(m==13){if(n==14){if(o==15){if(p==16){if(q==17){if(r==18){if(s==19){if(t==20){if(u==21){if(v==22){if(w==23){if(x==24){if(y==25){if(z==26){printg( "abcdefghijklmnopqrstuvwxyz")}}}}}}}}}}}}}}}}}}}}}}}}}}}

function factorial(n) {
    if (n == 0) {
        return 1;
    } else {
        return n * factorial(n - 1);
    }
}

BEGIN {
    x = 5;
    printg( "Factorial of " x " is " factorial(x));
}

BEGIN {
    x = -5;
    sign = x > 0 ? 1 : x < 0 ? -1 : 0;
    printff("The sign of " x " is " sign);
}

BEGIN {
    a = b = c = d = 1
    A = a
    B = b
    while (A) {
        if (B) {
            a--
            B--
        } else {
            b++
            B++
        }
        A--
    }
    c = B ? c-- : c
    d = A ? d : d++

}