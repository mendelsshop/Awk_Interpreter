(define (walk tree value)
  (cond
    ((null? tree) #f)
    ((null? (cdr tree)) (walk (car tree) value))
    ((< value (cadr tree)) (walk (car tree) value))
    ((> value (cadr tree)) (walk (cddr tree) value))
    ((= value (cadr tree)) #t)))

(define ones (list '() 2 '() 4 '() 6 '() 7 '() 8 '()))

(define sixties (list '() 52 '() 54 '() 66 '() 67 '() 68 '()))

(define tens (list ones 10 '() 30 '() 50 sixties 70 '() 90 '()))

(define ninehundreds (list '() 930 '() 940 '() 960 '() 970 '() 988 '()))

(define tree (list tens 100 '() 300 '() 500 '() 700 '() 900 ninehundreds))

(walk tree 0) ; returns false

(walk tree 6) ; returns true

(walk tree 67) ; returns true

(walk tree 750) ; returns false

(walk tree 970) ; returns true

(walk tree 900) ; returns true

(walk tree 1100) ; returns false
