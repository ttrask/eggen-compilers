%{
#include "sql.h"
extern int yylval;
%}
Delimiter    [ \t]
WhiteSpace   {Delimiter}+
Letter       [A-Za-z]
Digit        [0-9]
%%
{WhiteSpace}                  ;
{Digit}+                      {
                              
								yylval = atoi(yytext) ;
                               return val ;   
                              }
							  
"<"                          return Lt ;
">"                           return Gt ;
"<="                           return Lte ;
">="                           return Gte ;
"("                           return Lp ;
")"                           return Rp ;
\n                            return 0 ;
"CNO"						  return CNO ;	
"CITY"						  return CITY ;	
"CNAME"						  return CNAME ;	
"SNO"						  return SNO ;	
"PNO"						  return PNO ;	
"TQTY"						  return TQTY ;	
"SNAME"						  return SNAME ;	
"QUOTA"						  return QUOTA ;	
"PNAME"						  return PNAME ;	
"AVQTY"						  return COST ;	
"S#"						  return S_P ;	
"P#"						  return P_P ;	
"COLOR"						  return COLOR ;	
"WEIGHT"					  return WEIGHT ;	
"QTY"						  return QTY ;	
"["						  	  return Lb ;	
"]"						  	  return Rb ;	
"{"						  	  return Lk ;	
"}"						  	  return Rk ;	
","							  return Cm ;
"UNION"						  return UNION ;
"INTERSECT"					  return INTERSECT ;
"MINUS"						  return MINUS ;
"TIMES"						  return TIMES ;
"JOIN"						  return JOIN ;
"DIVIDEBY"					  return DIVIDEBY ;
"WHERE"						  return WHERE ;
"RENAME"					  return RENAME ;
"AS"					  	  return AS ;
"S"								return S;
"P"								return P;
"SP"							return SP;
"PRDCT"							return PRDCT;
"CUST"						return CUST;
"ORDERS"					return ORDERS;		

%%
