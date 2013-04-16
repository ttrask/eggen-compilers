%{
#include <stdio.h>
extern yylex();
extern yytext[];
%}
%start Program
%token Lt Gt Lte Gte Gt Eq Neq Lp Rp CNO CITY CNAME CUST COST
%token Op SNO PNO TQTY SNAME QUOTA PNAME AVQTY S_P
%token P_P COLOR WEIGHT QTY Lb Rb Lk Rk  
%token Cm UNION INTERSECT MINUS TIMES JOIN DIVIDEBY
%token WHERE RENAME AS S P val STATUS PRDCT ORDERS SP
%%


Program 	: expression  	{
							printf("ACCEPT");
						};

expression : one_relation_expression {
										printf("expression one_relationship\n");
								 };
		| two_relation_expression { printf("expression two_relationship\n");
								  };

one_relation_expression : renaming {printf("one_relation_expression renaming\n");
									};
						| restriction {printf("one_relation_expression restriction\n");
										};
						| projection	{printf("one_relation_expression projection\n");
										};
renaming 	: term RENAME attribute AS attribute { printf("renaming\n");
									};

term 		: relation 	{
							printf("term\n");
						};
			| Lp expression Rp	{
								};

restriction	: term WHERE comparison {
									};

projection  : term {};| term Lb attribute_commalist Rb {};

attribute_commalist : attribute 		{
										};
												
					| attribute Cm attribute_commalist

two_relation_expression : projection binary_operation expression	{
																	};

binary_operation : UNION {};
				| INTERSECT {};
				| MINUS {};
				| TIMES {};
				| JOIN {};
				| DIVIDEBY {};

comparison : attribute compare number {};

compare	: Lt {}; | Gt {}; | Lte {}; | Gte {}; | Eq {}; | Neq {}; 

number : val {}; | val number {}; 

attribute : CNO {printf("ATTRIBUTE\n");}; | CITY {printf("ATTRIBUTE\n");}; | CNAME {printf("ATTRIBUTE\n");}; | 
			SNO {printf("ATTRIBUTE\n");}; | PNO {printf("ATTRIBUTE\n");}; | TQTY {printf("ATTRIBUTE\n");}; | 
		  SNAME {printf("ATTRIBUTE\n");}; | QUOTA {printf("ATTRIBUTE\n");}; | PNAME {printf("ATTRIBUTE\n");}; | COST {printf("ATTRIBUTE\n");}; | AVQTY {printf("ATTRIBUTE\n");}; |
		  S_P {printf("ATTRIBUTE\n");}; | STATUS {printf("ATTRIBUTE\n");}; | P_P {printf("ATTRIBUTE\n");}; | COLOR {printf("ATTRIBUTE\n");}; | WEIGHT {printf("ATTRIBUTE\n");}; | QTY {printf("ATTRIBUTE\n");}; 

relation : S {}; | P {}; | SP {}; | PRDCT {}; | CUST {}; | ORDERS {}; 

%%
main()
{
   yyparse();
}
yyerror(char* error)
{
   printf(": error from yyerror\n");
   printf("%s\n", error);
   exit(0);
}
yywrap()
{
   printf("in yywarp\n");
   exit(0);
}