%{
    #include <bits/stdc++.h>
    #include <stdio.h>
    #include <stdlib.h>
    #include <string.h>
    using namespace std;
    void yyerror(const char *);
    int yylex(void);

    map<string, int> param_num; 
    map<string, string> macro_body;
    map<string, string> macro_type;

    

    char space = ' ';
    char* sp = &space;


    string trim(const string &s) { //referred online for optimizing this
        size_t start = s.find_first_not_of(" \t\r\n");
        size_t end   = s.find_last_not_of(" \t\r\n");
        if (start == string::npos) return "";
        return s.substr(start, end - start + 1);
    }

    string expand_macro_st(string body, vector<string> param) {
        
        string final_bod;
        string temp;
        stringstream ss(body);
        
        while (getline(ss, temp, ' ')) {
            int l = temp.length();
            
            if (l == 0) continue;
            if (temp[0] == '@' && temp[l - 1] == '@') {
                temp = temp.substr(1, l - 2);
                final_bod += param[stoi(temp)] + " ";
            }
            else {
                final_bod += temp + " ";
            }
        }
        
        return final_bod;
    }

    string expand_macro_exp(string body, vector<string> param) {
        
        string final_bod;
        string temp;
        stringstream ss(body);
      
        while (getline(ss, temp, ' ')) {
            int l = temp.length();
            if (l == 0) continue;
            if (temp[0] == '@' && temp[l - 1] == '@') {
                temp = temp.substr(1, l - 2);
                final_bod += param[stoi(temp)] + " ";
            }
            else {
                final_bod += temp + " ";
            }
        }
        
        return final_bod;
    }

    string open_and_tokenize(vector<string> params, string s) {
        if (params.size() == 0) return s;
        vector<string> v;
        string temp;
        stringstream ss(s);
        while (getline(ss, temp, ' ')) {
            if (temp != "") {
                int x = find(params.begin(), params.end(), trim(temp)) - params.begin();
                if (x == params.size()) v.push_back(temp);
                else {
                    v.push_back("@" + to_string(x) + "@");
                } 
            }
        }
        string res;
        for (auto x: v) res += x + " ";
        return res;
    }

    string concat_char(vector<char*> &charList) {
        string s;
        int n = charList.size();
        for (auto x: charList) {
            s += string(x);
        }
        return s;
    }

    vector<string> extract_params(string stream) {
        stringstream ss(stream);
        vector<string> params;
        string temp;
        while (getline(ss, temp, ',')) {
            temp = trim(temp);
            if (temp != "") params.push_back(temp);
        }
        return params;
    }

%}

%define parse.trace
%union {
    char* text;
    int val;
}

%token <text> IDENTIFIER
%token <text> NUMBER
%token <text> CLASS EXTENDS PUBLIC STATIC VOID MAIN STRING RETURN H_DEF IMPORT JAVA FUNC_SMALL UTIL FUNC
%token <text> INT BOOLEAN IF ELSE WHILE DO SYSTEM OUT PRINTLN TRU FAL LENGTH THIS NEW LAMBDA_ARROW
%token <text> AND OR NE LE GE AR
%type <text> Goal MainClass TypeDec_star ImportFunction '{' '}' '(' ')' '[' ']' '.' ',' ';' '<' '>' '=' '*' '/' '-' '+' '!'
%type <text> Identifier Expression TypeDeclaration Met_Dec_star MethodDeclaration Extend_args Extend_expr Extend_ident Type 
%type <text> Stat_star Statement Matched Unmatched Others PrimaryExpression LambdaExpression Type_Ident_star MacroDef_star MacroDefExpression MacroDefinition MacroDefStatement Integer
/* %left '+' '-' 
%left '*' '/'  //  (* and / have higher precedence that + and -)
%left '<' '>' '.' NE LE GE
%right '=' */

/* %debug
%error-verbose */

%%
Goal: 
    MacroDef_star MainClass TypeDec_star                                        { vector<char*> v = {$1, $2, $3}; string s = concat_char(v) + "\n " ; $$ = strdup(s.c_str()); printf("%s", $$);}
    | ImportFunction MacroDef_star MainClass TypeDec_star                       { vector<char*> v = {$1, $2, $3, $4}; string s = concat_char(v) + "\n " ; $$ = strdup(s.c_str()); printf("%s", $$);}
    ;

ImportFunction:
    IMPORT JAVA '.' UTIL '.' FUNC_SMALL '.' FUNC ';'                                                             { vector<char*> v = {$1, sp, $2, $3, $4, $5, $6, $7, $8, $9}; string s = concat_char(v) + "\n"; $$ = strdup(s.c_str()); }                                                
    ;

MainClass: 
    CLASS Identifier '{' PUBLIC STATIC VOID MAIN '(' STRING '[' ']' Identifier ')' '{' SYSTEM '.' OUT '.' PRINTLN '(' Expression ')' ';' '}' '}'
                                                                               { string temp4 = string($3) + "\n ",temp3 = string($14) + "\n ", temp2 = string($24)+ "\n ", temp1 = string($23)+ "\n "; vector<char*> v = {$1, sp, $2, sp, strdup(temp4.c_str()), $4, sp, $5, sp, $6, sp, $7, $8, sp, $9, $10, $11, sp, $12, sp, $13, sp, strdup(temp3.c_str()), $15, $16, $17, $18, $19, $20, sp, $21, sp, $22, strdup(temp1.c_str()), strdup(temp2.c_str()), $25}; string s = concat_char(v)+ "\n " ; $$ = strdup(s.c_str());}
    ;

TypeDec_star:
    TypeDec_star TypeDeclaration                                               { vector<char*> v = {$1, $2}; string s = concat_char(v) ; $$ = strdup(s.c_str());}
    | /* empty */                                                              { string s = ""; $$ = strdup(s.c_str());}
    ;

TypeDeclaration:
    CLASS Identifier '{' Type_Ident_star Met_Dec_star '}'                      { string temp = string($3) + "\n "; vector<char*> v = {$1, sp, $2, sp, strdup(temp.c_str()), $4, $5, $6}; string s = concat_char(v)+ "\n " ; $$ = strdup(s.c_str());}
    | CLASS Identifier EXTENDS Identifier '{' Type_Ident_star Met_Dec_star '}' { string temp = string($5) + "\n "; vector<char*> v = {$1, sp, $2, sp, $3, sp, $4, sp, strdup(temp.c_str()), $6, $7, $8}; string s = concat_char(v)+ "\n "; $$ = strdup(s.c_str());}
    ;

Met_Dec_star:
    Met_Dec_star MethodDeclaration                                         { vector<char*> v = {$1, $2}; string s = concat_char(v) ; $$ = strdup(s.c_str());}
    | /* empty */                                                          {  string s = ""; $$ = strdup(s.c_str());}
    ;

Extend_args:
    Extend_args ',' Type Identifier                                        { vector<char*> v = {$1, sp, $2, sp, $3, sp, $4}; string s = concat_char(v); $$ = strdup(s.c_str());}
    | /* empty */                                                          { string s = ""; $$ = strdup(s.c_str());}
    ;

MethodDeclaration:
    PUBLIC Type Identifier '(' ')' '{' Type_Ident_star Stat_star RETURN Expression ';' '}'
                                                                           { string oB = string($6) + "\n ",temp = string($11) + "\n "; vector<char*> v = {$1, sp, $2, sp, $3, $4, $5, sp, strdup(oB.c_str()), $7, $8, $9, sp, $10, sp, strdup(temp.c_str()), $12}; string s = concat_char(v)+ "\n " ; $$ = strdup(s.c_str());}
    | PUBLIC Type Identifier '(' Type Identifier Extend_args ')' '{' Type_Ident_star Stat_star RETURN Expression ';' '}'
                                                                           { string oB = string($9) + "\n ",temp = string($14) + "\n "; vector<char*> v = {$1, sp, $2, sp, $3, $4, sp, $5, sp, $6, sp, $7, $8, sp, strdup(oB.c_str()), $10, $11, $12, sp, $13, sp, strdup(temp.c_str()), $15}; string s = concat_char(v)+ "\n " ; $$ = strdup(s.c_str());}
    ;

Stat_star:
    Statement Stat_star                                                    { vector<char*> v = {$1, $2}; string s = concat_char(v); $$ = strdup(s.c_str());}
    | /* empty */                                                          { string s = ""; $$ = strdup(s.c_str());}
    ;

Statement: 
    Matched                                                                { $$ = $1;}
    | Unmatched                                                            { $$ = $1;}
    ;

Matched:
    IF '(' Expression ')' Matched ELSE Matched                            { vector<char*> v = {$1, $2, sp, $3, sp, $4, sp, $5, $6, sp, $7}; string s = concat_char(v); $$ = strdup(s.c_str());}
    | Others                                                             { $$ = $1;}
    ;
Unmatched:
    IF '(' Expression ')' Statement                                       { vector<char*> v = {$1, $2, sp, $3, sp, $4, sp, $5}; string s = concat_char(v); $$ = strdup(s.c_str());}
    | IF '(' Expression ')' Matched ELSE Unmatched                        { vector<char*> v = {$1, $2, sp, $3, sp, $4, sp, $5, $6, sp, $7}; string s = concat_char(v); $$ = strdup(s.c_str());}
    
    ;

Others:
    '{' Stat_star '}'                                                     { string temp = string($1) + "\n "; vector<char*> v = {strdup(temp.c_str()), $2, sp, $3, sp}; string s = concat_char(v) + "\n " ; $$ = strdup(s.c_str());}
    | SYSTEM '.' OUT '.' PRINTLN '(' Expression ')' ';'                   { vector<char*> v = {$1, $2, $3, $4, $5, $6, sp, $7, sp, $8, sp, $9, sp}; string s = concat_char(v)+ "\n " ; $$ = strdup(s.c_str());}
    | Identifier '=' Expression ';'                                       { vector<char*> v = {$1, sp, $2, sp, $3, sp, $4, sp}; string s = concat_char(v) + "\n "  ; $$ = strdup(s.c_str());}
    | Identifier '[' Expression ']' '=' Expression ';'                    { vector<char*> v = {$1, $2, $3, $4, sp, $5, sp, $6, sp, $7, sp}; string s = concat_char(v) + "\n " ; $$ = strdup(s.c_str());}
    | WHILE '(' Expression ')' Statement                                { vector<char*> v = {$1, $2, sp, $3, sp, $4, sp, $5}; string s = concat_char(v) ; $$ = strdup(s.c_str());}
    | DO Statement WHILE Expression ';'                                   { vector<char*> v = {$1, sp, $2, $3, sp, $4, sp, $5, sp}; string s = concat_char(v)+ "\n " ; $$ = strdup(s.c_str());} 
    | Identifier '(' ')' ';'                                              { 
                                                                            if (param_num.find(string($1)) == param_num.end()) yyerror(sp);
                                                                            if (param_num[string($1)] != 0) yyerror(sp);
                                                                            if (macro_type[string($1)] != "st") yyerror(sp);
                                                                            string s = expand_macro_st(macro_body[string($1)], {}) + " ";
                                                                            s = "{" +  s  + "} ";
                                                                            $$ = strdup(s.c_str()); 
                                                                          }
    | Identifier '(' Expression Extend_expr ')' ';'                       {     
                                                                            vector<string> params = extract_params(string($3) + string($4));
                                                                            if (param_num.find(string($1)) == param_num.end()) yyerror(sp);
                                                                            if (param_num[string($1)] != params.size()) yyerror(sp);
                                                                            if (macro_type[string($1)] != "st") yyerror(sp);
                                                                            string s = expand_macro_st(macro_body[string($1)], params) + " ";
                                                                            s = "{" +  s  + "} ";
                                                                            $$ = strdup(s.c_str()); 
                                                                          }
    ;

Extend_expr:
    Extend_expr ',' Expression                                            { vector<char*> v = {$1, sp, $2, sp, $3}; string s = concat_char(v); $$ = strdup(s.c_str());}
    | /* empty */                                                         { string s = ""; $$ = strdup(s.c_str());}
    ;

Expression:
    PrimaryExpression AND PrimaryExpression                               { vector<char*> v = {$1, sp, $2, sp, $3, sp}; string s = concat_char(v) ; $$ = strdup(s.c_str());}
    | PrimaryExpression OR PrimaryExpression                              { vector<char*> v = {$1, sp, $2, sp, $3, sp}; string s = concat_char(v) ; $$ = strdup(s.c_str());}
    | PrimaryExpression NE PrimaryExpression                              { vector<char*> v = {$1, sp, $2, sp, $3, sp}; string s = concat_char(v) ; $$ = strdup(s.c_str());}
    | PrimaryExpression LE PrimaryExpression                              { vector<char*> v = {$1, sp, $2, sp, $3, sp}; string s = concat_char(v) ; $$ = strdup(s.c_str());}
    | PrimaryExpression '+' PrimaryExpression                             { vector<char*> v = {$1, sp, $2, sp, $3, sp}; string s = concat_char(v) ; $$ = strdup(s.c_str());}
    | PrimaryExpression '-' PrimaryExpression                             { vector<char*> v = {$1, sp, $2, sp, $3, sp}; string s = concat_char(v) ; $$ = strdup(s.c_str());}
    | PrimaryExpression '*' PrimaryExpression                             { vector<char*> v = {$1, sp, $2, sp, $3, sp}; string s = concat_char(v) ; $$ = strdup(s.c_str());} 
    | PrimaryExpression '/' PrimaryExpression                             { vector<char*> v = {$1, sp, $2, sp, $3, sp}; string s = concat_char(v) ; $$ = strdup(s.c_str());}
    | PrimaryExpression '[' PrimaryExpression ']'                         { vector<char*> v = {$1, $2, sp, $3, sp, $4, sp}; string s = concat_char(v) ; $$ = strdup(s.c_str());}                
    | PrimaryExpression '.' Identifier '(' ')'                            { vector<char*> v = {$1, $2, $3, $4, $5}; string s = concat_char(v) ; $$ = strdup(s.c_str());}
    | PrimaryExpression '.' Identifier '(' Expression Extend_expr ')'     { vector<char*> v = {$1, $2, $3, $4, sp, $5, sp, $6, sp, $7, sp}; string s = concat_char(v) ; $$ = strdup(s.c_str());}
    | PrimaryExpression '.' LENGTH                                        { vector<char*> v = {$1, $2, $3, sp}; string s = concat_char(v) ; $$ = strdup(s.c_str());}
    | PrimaryExpression                                                   { string s = string($1) ; $$ = strdup(s.c_str()); }
    | Identifier '(' ')'                                                  { 
                                                                            if (param_num.find(string($1)) == param_num.end()) yyerror(sp);
                                                                            if (param_num[string($1)] != 0) yyerror(sp);
                                                                            if (macro_type[string($1)]!= "exp") yyerror(sp);
                                                                            string s = expand_macro_exp(macro_body[string($1)], {}) + " ";
                                                                            s = "(" +  s  + ") ";
                                                                            $$ = strdup(s.c_str());
                                                                          }
    | Identifier '(' Expression Extend_expr ')'                           { vector<string> params = extract_params(string($3) + string($4));
                                                                            int l = params.size();
                                                                            for (int i = 0; i < l; i++) {
                                                                                ostringstream oss;
                                                                                oss << "(" << params[i] << ")";
                                                                                params[i] = oss.str(); 
                                                                            }
                                                                            if (param_num.find(string($1)) == param_num.end()) yyerror(sp);
                                                                            if (param_num[string($1)] != params.size()) yyerror(sp);
                                                                            if (macro_type[string($1)] != "exp") yyerror(sp);
                                                                            string s = expand_macro_exp(macro_body[string($1)], params) + " ";
                                                                            s = "(" +  s  + ") ";
                                                                            $$ = strdup(s.c_str()); 
                                                                            
                                                                            }
    | LambdaExpression                                                    { string s = string($1) ; $$ = strdup(s.c_str()); }
    ;

LambdaExpression:
    '(' IDENTIFIER LAMBDA_ARROW  Expression                                 { vector<char*> v = {$1, sp, $2, sp, $3, sp, $4}; string s = concat_char(v) ; $$ = strdup(s.c_str());}

Type:
    INT '[' ']'                                                           { vector<char*> v = {$1, $2, $3}; string s = concat_char(v); $$ = strdup(s.c_str());}                                                       
    | BOOLEAN                                                             { $$ = $1;}
    | INT                                                                 { $$ = $1;}
    | Identifier                                                          { $$ = $1;}
    | FUNC '<' Type ',' Type '>'                                          { vector<char*> v = {$1, $2, $3, $4, $5, $6}; string s = concat_char(v); $$ = strdup(s.c_str());}
    ;

PrimaryExpression:
    Integer                                                               { $$ = $1;}
    | TRU                                                                 { $$ = $1;}
    | FAL                                                                 { $$ = $1;}
    | Identifier                                                          { $$ = $1;}
    | THIS                                                                { $$ = $1;}
    | NEW INT '[' Expression ']'                                          { vector<char*> v = {$1, sp, $2, $3, sp, $4, sp, $5}; string s = concat_char(v); $$ = strdup(s.c_str());}
    | NEW Identifier '(' ')'                                              { vector<char*> v = {$1, sp, $2, $3, $4}; string s = concat_char(v); $$ = strdup(s.c_str());}
    | '!' Expression                                                      { vector<char*> v = {$1, $2}; string s = concat_char(v); $$ = strdup(s.c_str());}
    | '(' Expression ')'                                                  { vector<char*> v = {$1, sp, $2, sp, $3}; string s = concat_char(v); $$ = strdup(s.c_str());}
    ;

Type_Ident_star:
    Type_Ident_star Type Identifier ';'                                   { vector<char*> v = {$1, sp, $2, sp, $3, sp, $4}; string s = concat_char(v) + "\n "; $$ = strdup(s.c_str()); }
    | /* empty */                                                         { string s = ""; $$ = strdup(s.c_str()); }
    ;

MacroDef_star:
    MacroDef_star MacroDefinition                                          { vector<char*> v = {$1, sp, $2}; string s = concat_char(v); $$ = strdup(s.c_str()); }
    | /* empty */                                                          { string s = ""; $$ = strdup(s.c_str()); }   
    ;   

MacroDefinition:
    MacroDefExpression                                                     {$$ = $1;}
    | MacroDefStatement                                                    {$$ = $1;}
    ;

Extend_ident:
    Extend_ident ',' Identifier                                            { vector<char*> v = {$1, sp, $2, sp, $3}; string s = concat_char(v); $$ = strdup(s.c_str()); }
    | /* empty */                                                          { string s = ""; $$ = strdup(s.c_str()); }
    ;
    
MacroDefStatement:
    H_DEF Identifier '(' ')' '{' Stat_star '}'                             {    string body = open_and_tokenize({}, string($5) + "\n " + string($6) +" " + string($7) + "\n ");  
                                                                                macro_body[string($2)] = body; param_num[string($2)] = 0; macro_type[string($2)] = "st";
                                                                                string temp = string($5) + "\n "; 
                                                                        
                                                                                string s = "";
                                                                                $$ = strdup(s.c_str());
                                                                           }                          
    | H_DEF Identifier '(' Identifier Extend_ident ')' '{' Stat_star '}'   {    
                                                                                vector<string> params = extract_params(string($4) + string($5));
                                                                                string temp;
                                                                                string body = open_and_tokenize(params, string($7) + "\n " + string($8) +" " + string($9) + "\n ");
                                                                                macro_body[string($2)] = body;
                                                                                param_num[string($2)] = params.size();
                                                                                macro_type[string($2)] = "st";
                                                                                temp = string($7) + "\n "; 
                                                                                
                                                                                string s = "";
                                                                                $$ = strdup(s.c_str());
                                                                            }
    ;

MacroDefExpression:
    H_DEF Identifier '(' ')' '(' Expression ')'                            {    string body = open_and_tokenize({}, string($5) + " " + string($6) + " " + string($7)); 
                                                                                param_num[string($2)] = 0; macro_body[string($2)] = body;  
                                                                                macro_type[string($2)] = "exp";
                                                                                string s = "";
                                                                                $$ = strdup(s.c_str()); 
                                                                            }
    | H_DEF Identifier '(' Identifier Extend_ident ')' '(' Expression ')'  { 
                                                                                vector<string> params = extract_params(string($4) + string($5));
                                                                               
                                                                                string body = open_and_tokenize(params, string($7) + " " + string($8) + " " + string($9));
                                                                                macro_body[string($2)] = body;
                                                                                param_num[string($2)] = params.size();
                                                                                macro_type[string($2)] = "exp";
                                                                                string s = "";
                                                                                $$ = strdup(s.c_str());
                                                                                
                                                                           }
    ;

Identifier:
    IDENTIFIER {$$ = $1;}
    | JAVA {$$ = $1;}
    | FUNC {$$ = $1;}
    | FUNC_SMALL {$$ = $1;}
    | UTIL {$$ = $1;}
    ;

Integer:
    NUMBER {$$ = $1;}
    ;

%%
extern int yydebug;

void yyerror(const char *s) {
    fprintf(stdout, "%s\n", "// Failed to parse macrojava code.\n");
    exit(1);
}

int main(void) {
    #ifdef YYDEBUG
        yydebug = 0;
    #endif

    return yyparse();
}
