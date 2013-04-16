
<!-- saved from url=(0076)http://www.unf.edu/public/cop4620/ree/Examples/LEXYACC_sample/SimpCalc/p58.y -->
<html><head><meta http-equiv="Content-Type" content="text/html; charset=UTF-8"></head><body><div id="StayFocusd-infobar" style="display:none;"><img src="chrome-extension://laankejkbhbdhmipfmgcngdelahlfoji/img/eye_19x19_red.png"><span id="StayFocusd-infobar-msg"></span><span id="StayFocusd-infobar-links"><a href="http://www.unf.edu/public/cop4620/ree/Examples/LEXYACC_sample/SimpCalc/p58.y#" id="StayFocusd-infobar-never-show">hide forever</a>&nbsp;&nbsp;|&nbsp;&nbsp;<a href="http://www.unf.edu/public/cop4620/ree/Examples/LEXYACC_sample/SimpCalc/p58.y#" id="StayFocusd-infobar-hide">hide once</a></span></div><pre style="word-wrap: break-word; white-space: pre-wrap;">%token NAME NUMBER

%%

statement: NAME '=' expression     { printf("Yaa I found NAME\n %s\n", $1);}
         | expression              { printf("=%d\n", $1); }
         ;

expression: expression '+' NUMBER  { $$ = $1 + $3; }
          | expression '-' NUMBER  { $$ = $1 - $3; }
          | NUMBER                 { $$ = $1; }
          ;
%%
yyerror()
{
   printf("error from yyerror\n");
   exit(0);
}

main()
{
   yyparse();
}

yywrap()
{
}

</pre></body></html>