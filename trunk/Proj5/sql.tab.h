#ifndef YYATTRIBUTETYPE
#define YYATTRIBUTETYPE
enum yyattributetype{
Id,
Lt,
Gt,
Lte,
Gte,
Gt,
Lp,
Rp,
CNO,
CITY,
CNAME,
Op,
SNO,
PNO,
TQTY,
SNAME,
QUOTA,
PNAME,
AVQTY,
S_P
P_P,
COLOR,
WEIGHT,
QTY,
Lb,
Rb,
Lk,
Rk,
CNO,
Cm,
UNION,
INTERSECT,
MINUS,
TIMES,
JOIN,
DIVIDEBY,WHERE,
RENAME,
AS
};

#endif
#define Lt
#define Gt
#define Lte
#define Gte
#define Gt
#define Lp
#define Rp
#define CNO
#define CITY
#define CNAME
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
#define CNO
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


#if ! defined (YYSTYPE) && ! defined (YYSTYPE_IS_DECLARED)
typedef int YYSTYPE;
#define yystype YYSTYPE
#define YYSTYPE_IS_DECLARED 1
#define YYSTYPE_IS_TRIVIAL 1
#endif

extern YYSTYPE yylval;
