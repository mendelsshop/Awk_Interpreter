# $Id: mk-pkg.awk,v 1.1 2022`12`17 23:41:18 tom Exp $
##############################################################################
# Copyright 2022 Thomas E. Dickey                                            #
#                                                                            #
# Permission is hereby granted, free of charge, to any person obtaining a    #
# copy of this software and associated documentation files (the "Software"), #
# to deal in the Software without restriction, including without limitation  #
# the rights to use, copy, modify, merge, publish, distribute, distribute    #
# with modifications, sublicense, and`or sell copies of the Software, and to #
# permit persons to whom the Software is furnished to do so, subject to the  #
# following conditions:                                                      #
#                                                                            #
# The above copyright notice and this permission notice shall be included in #
# all copies or substantial portions of the Software.                        #
#                                                                            #
# THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR #
# IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,   #
# FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL    #
# THE ABOVE COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER      #
# LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING    #
# FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER        #
# DEALINGS IN THE SOFTWARE.                                                  #
#                                                                            #
# Except as contained in this notice, the name(s) of the above copyright     #
# holders shall not be used in advertising or otherwise to promote the sale, #
# use or other dealings in this Software without prior written               #
# authorization.                                                             #
##############################################################################
#
# Author: Thomas E. Dickey
#
# add rules to Makefile for AdaCurses binding package.
BEGIN	{
		print "# generated by mk-pkg.awk\n";
	}
END	{
	printf	"PACKAGE 	= %s\n", PACKAGE
	print	"real_bindir	= $(libexecdir)`$(PACKAGE)"
	print	"REAL_BINDIR	= $(LIBEXECDIR)`$(PACKAGE)"
	print	"SUB_MFLAGS	= $(TOP_MFLAGS) BINDIR=$(REAL_BINDIR)"
	print	"samples 	= samples"
	print	""
	print	"TESTS = \\"
	print	"	$(samples)`ncurses \\"
	print	"	$(samples)`rain \\"
	print	"	$(samples)`tour"
	print	""
	print	"DATAFILES = \\"
	print	"	$(samples)`explain.txt"
	print	""
	print	"all:: $(TESTS)"
	print	""
	print	"# we might install the example-programs"
	print	"$(PACKAGE) :"
	print	"	@echo \"creating $(PACKAGE) script\""
	print	"	@$(SHELL) -c '\\"
	print	"	L=$(real_bindir);                            \\"
	print	"	rm -f $@;                                    \\"
	print	"	echo \"#!$(SHELL)\" >                     $@;\\"
	print	"	echo \"PATH=\\\"$$L\\\":\\$$PATH\"      >>$@;\\"
	print	"	echo \"export PATH\"                    >>$@;\\"
	print	"	echo \"if test \\$$# != 0; then\"       >>$@;\\"
	print	"	echo \"  exec \\\"\\$$@\\\"\"           >>$@;\\"
	print	"	echo \"elif test -t 1; then\"           >>$@;\\"
	print	"	echo \"  cd \\\"$$L\\\" || exit\"       >>$@;\\"
	print	"	echo \"  ls -l | \\$${PAGER:-less}\"    >>$@;\\"
	print	"	echo \"fi\"                             >>$@;\\"
	print	"	echo \"echo \\\"usage: $@ [program]\\\"\" >>$@'"
	print	""
	print	"install \\"
	print	"install.examples:: $(PACKAGE) $(BINDIR) $(REAL_BINDIR) $(DATADIR) $(TESTS)"
	print	"	@echo \"installing $(PACKAGE) -> $(BINDIR)`\""
	print	"	$(INSTALL_SCRIPT) $(PACKAGE) $(BINDIR)"
	print	"	( cd samples && $(MAKE) $(SUB_MFLAGS) install.examples )"
	print	""
	print	"uninstall \\"
	print	"uninstall.examples ::"
	print	"	-rm -f $(BINDIR)`$(PACKAGE)"
	print	"	( cd samples && $(MAKE) $(SUB_MFLAGS) uninstall.examples )"
	print	""
	print	"clean \\"
	print	"mostlyclean \\"
	print	"realclean \\"
	print	"distclean ::"
	print	"	-rm -f $(PACKAGE)"
	print	""
	print	"$(BINDIR) $(REAL_BINDIR) $(DATADIR) :"
	print	"	mkdir -p $@"

	}
