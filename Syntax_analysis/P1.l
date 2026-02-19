%{
    #include <bits/stdc++.h>
    #include "P1.tab.h"
    void yyerror(char *);
    int line_counter = 1;
    using namespace std;
%}

WS              [ \t\n\r]
SL_COMMENT      \/\/[^\n]*
ML_COMMENT      \/\*([^*]|\n|\*+[^*/])*\*\/
SKIP            ({WS}|{SL_COMMENT}|{ML_COMMENT})
IDENT           [a-zA-Z_$][a-zA-Z0-9_$]*


%%


"import"      { yylval.text = strdup(yytext); return IMPORT; }
"java"        { yylval.text = strdup(yytext); return JAVA;}
"util"        { yylval.text = strdup(yytext); return UTIL;}
"function"    {yylval.text = strdup(yytext); return FUNC_SMALL;}
"class"                                   {  yylval.text = strdup(yytext); return CLASS;}
"extends"                                 {  yylval.text = strdup(yytext); return EXTENDS;}
"public"                                  {  yylval.text = strdup(yytext); return PUBLIC;}
"static"                                  {  yylval.text = strdup(yytext);  return STATIC;}
"void"                                    {  yylval.text = strdup(yytext);  return VOID;}
"main"                                    {  yylval.text = strdup(yytext); return MAIN;}
"String"                                  {  yylval.text = strdup(yytext); return STRING;}
"return"                                  {  yylval.text = strdup(yytext); return RETURN;}
"int"                                     {  yylval.text = strdup(yytext); return INT;}
"boolean"                                 {  yylval.text = strdup(yytext); return BOOLEAN;}
"if"                                      {  yylval.text = strdup(yytext); return IF;}
"else"                                    {  yylval.text = strdup(yytext); return ELSE;}
"while"                                   {  yylval.text = strdup(yytext); return WHILE;}
"do"                                      {  yylval.text = strdup(yytext); return DO;}
"System"                                  {  yylval.text = strdup(yytext); return SYSTEM;}
"out"                                     {  yylval.text = strdup(yytext); return OUT;}
"println"                                 {  yylval.text = strdup(yytext); return PRINTLN;}
"true"                                    {  yylval.text = strdup(yytext); return TRU;}
"false"                                   {  yylval.text = strdup(yytext); return FAL;}
"length"                                  {  yylval.text = strdup(yytext); return LENGTH;}
"this"                                    {  yylval.text = strdup(yytext); return THIS;}
"new"                                     {  yylval.text = strdup(yytext); return NEW;}
"#define"                                 {  yylval.text = strdup(yytext); return H_DEF;}

")"{SKIP}*"->"                            { 
                                              yylval.text = strdup(yytext);
                                              return LAMBDA_ARROW;
                                          }


"("                                       {  yylval.text = strdup(yytext); return '(';}
")"                                       {  yylval.text = strdup(yytext); return ')';}
"*"                                       {  yylval.text = strdup(yytext); return '*';}
"/"                                       {  yylval.text = strdup(yytext); return '/';}
"+"                                       {  yylval.text = strdup(yytext); return '+';}
"-"                                       {  yylval.text = strdup(yytext); return '-';}
"&&"                                      {  yylval.text = strdup(yytext); return AND;}
"||"                                      {  yylval.text = strdup(yytext); return OR;}
"."                                       {  yylval.text = strdup(yytext); return '.';}
"="                                       {  yylval.text = strdup(yytext); return '=';}
"!"                                       {  yylval.text = strdup(yytext); return '!';}
"!="                                      {  yylval.text = strdup(yytext); return NE;}
"<"                                       {  yylval.text = strdup(yytext); return '<';}
"<="                                      {  yylval.text = strdup(yytext); return LE;}
">"                                       {  yylval.text = strdup(yytext); return '>';}
">="                                      {  yylval.text = strdup(yytext); return GE;}
"->"                                      {  yylval.text = strdup(yytext); return AR;}
"{"                                       {  yylval.text = strdup(yytext); return '{';}
"}"                                       {  yylval.text = strdup(yytext); return '}';}
"["                                       {  yylval.text = strdup(yytext); return '[';}
"]"                                       {  yylval.text = strdup(yytext); return ']';}
";"                                       {  yylval.text = strdup(yytext); return ';';}
","                                       {  yylval.text = strdup(yytext); return ',';}

"Function"                                {  yylval.text = strdup(yytext); return FUNC;}

[0-9]+                                    {  yylval.text = strdup(yytext); return NUMBER; }
[a-zA-Z_$][a-zA-Z0-9_$]*                  {  yylval.text = strdup(yytext); return IDENTIFIER; }
"//"[^\n]*                                {  }
"/*"([^*]|\n|\*+[^*/])*"*/"               {  }




[ \t]                                     { }
\r                                        { }
\n                                        { line_counter++;}
.                                         { printf("Lexing Error\n");}

%%

int yywrap(void) {
    yylval.text = strdup(yytext); return 1;
}

// int main() {
//   yylex();
//   yylval.text = strdup(yytext); return 0;
// }