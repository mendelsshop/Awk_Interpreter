# test for awk
# parses csv prints non empty fields in each line in reverse order

BEGIN { FS=","}
{
    k = 0
    for (i = 1; i <= NF; i++) {
        if (!$i == 0) {
            array[++k] = $i;
        }

    }



    len = array_length(array);
    for (i = 1; i <= len; i++) {
        printf "%s", array[len - i];
    }
    print

    delete array;
}

function array_length(array) {
    len = 0;
    for (i in array) {
        len++;
    }
    return len;
}

