#ifndef YYATTRIBUTETYPE
#define YYATTRIBUTETYPE
enum yyattributetype{
Id
,Lt
,Gt
,Lte
,Gte
,Eq
,Neq
,Lp
,Rp
,CNO
,CITY
,CNAME
,CUST
,COST
,Op
,SNO
,PNO
,TQTY
,SNAME
,QUOTA
,PNAME
,AVQTY
,S_P
,P_P
,COLOR
,WEIGHT
,QTY
,Lb
,Rb
,Lk
,Rk
,Cm
,UNION
,INTERSECT
,MINUS
,TIMES
,JOIN
,DIVIDEBY,
WHERE
,RENAME
,AS
,S
,P
,val
,STATUS
,PRDCT
,ORDERS
,SP
};

#endif
#define Id
#define Lt
#define Gt
#define Lte
#define Gte
#define Gt
#define Eq
#define Neq
#define Lp
#define Rp
#define CNO
#define CITY
#define CNAME
#define CUST
#define COST
#define Op
#define SNO
#define PNO
#define TQTY
#define SNAME
#define QUOTA
#define PNAME
#define AVQTY
#define S_P
#define P_P
#define COLOR
#define WEIGHT
#define QTY
#define Lb
#define Rb
#define Lk
#define Rk
#define Cm
#define UNION
#define INTERSECT
#define MINUS
#define TIMES
#define JOIN
#define DIVIDEBY
#define WHERE
#define RENAME
#define AS
#define S
#define P
#define val
#define STATUS
#define PRDCT
#define ORDERS
#define SP

#if ! defined (YYSTYPE) && ! defined (YYSTYPE_IS_DECLARED)
typedef int YYSTYPE;
#define yystype YYSTYPE
#define YYSTYPE_IS_DECLARED 1
#define YYSTYPE_IS_TRIVIAL 1
#endif

extern YYSTYPE yylval;
