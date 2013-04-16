
<!-- saved from url=(0076)http://www.unf.edu/public/cop4620/ree/Examples/LEXYACC_sample/SimpCalc/p58.l -->
<html><head><meta http-equiv="Content-Type" content="text/html; charset=UTF-8"></head><body><div id="StayFocusd-infobar" style="display:none;"><img src="chrome-extension://laankejkbhbdhmipfmgcngdelahlfoji/img/eye_19x19_red.png"><span id="StayFocusd-infobar-msg"></span><span id="StayFocusd-infobar-links"><a href="http://www.unf.edu/public/cop4620/ree/Examples/LEXYACC_sample/SimpCalc/p58.l#" id="StayFocusd-infobar-never-show">hide forever</a>&nbsp;&nbsp;|&nbsp;&nbsp;<a href="http://www.unf.edu/public/cop4620/ree/Examples/LEXYACC_sample/SimpCalc/p58.l#" id="StayFocusd-infobar-hide">hide once</a></span></div><pre style="word-wrap: break-word; white-space: pre-wrap;">%{
#include "p58.tab.h"
extern int yylval;
%}

%%
[0-9]+        { yylval = atoi(yytext); return NUMBER; }
[ \t] ;      /* ignore whitespace */
\n            return 0;   /* logical EOF */
[A-Z]+        { {printf("%c %d\n",yytext[0],yylval) ; /*
                            return yytext[0];*/}
                 strcpy(yylval, yytext); return NAME; }
.             {printf("%c\n",yytext[0]) ; return yytext[0];}
%%
</pre></body></html>