BEGIN {
    env[0]
}

{
   print eval($0)
}

function eval(expr) {
        if (expr ~  /^[0-9]+$/) {
            # print "number"
            return expr
        }
        if (expr ~ /^[a-z]+$/) 
        {
            # print "symbol"
            return env[expr];
        }
        if (expr ~ /^".+"$/) {
            # print "string"
            return substr(expr, 2, length(expr) - 2);
        }
        if (expr ~ /\(.+\)/) {
            # print "list"
            expr = substr(expr, 2, length(expr) - 2);
            a[0]
            split(expr, a, " ");
   
            if (a[1] == "display") print eval(a[2]);
            else if (a[1] == "set!") env[a[2]] = eval(a[3]);
            else if (a[1] == "error") print "error", eval(a[2]);
            else if (a[1] == "define") {
                if (a[2] ~ /\(.*\)/) {
                    eval("(error \"define: bad syntax\")");
                } else {
                    env[a[2]] = eval(a[3]);
                }
            }
            else if (a[1] == "lambda") {
                eval("(error \"lambda: bad syntax\")");
            }
            # + - / *
            
            else if (a[1] == "+") return eval(a[2]) + eval(a[3]);
            else if (a[1] == "-") return eval(a[2]) - eval(a[3]);
            else if (a[1] == "*") return eval(a[2]) * eval(a[3]);
            else if (a[1] == "/") return eval(a[2]) / eval(a[3]);
            else {
                printf "error %s: bad syntax", a[1];
                return;
            }
        } else {
        print "error"}
}
