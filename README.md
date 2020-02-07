# FuzzyRegex
2012 - Fuzzy regex pattern matching and capturing.


## Combines the matching and capturing power of Regex with the comparison power of the Levenshtein Distance algorithm.


### Match strings to patterns and extract variables even if the input text does not match the pattern exactly. For example:

__Pattern:__ "my name is ¿ and I am ¿ years old"

__Input:__ "my name is John and I am 30 years old"\
    Score: 1.0\
    Variables: ["John", "30"]\
    Tokens: ["my name is ", " and I am ", " years old"]

__Input:__ "My names John and I'm 30 years old."\
    Score: 0.8285714285714286\
    Variables: ["John", "30"]\
    Tokens: ["My names ", " and I'm ", " years old."]

### In ambiguous cases all valid results are returned. For example:

__Pattern:__ "What ¿ ¿s"

__Input:__ "What the hell are lobsters"\
    Score: 1.0\
    Extraction 1:\
        Variables: ["the", "hell are lobster"]\
        Tokens: ["What ", " ", "s"]\
    Extraction 2:\
        Variables: ["the hell are", "lobster"]\
        Tokens: ["What ", " ", "s"]\
    Extraction 3:\
        Variables: ["the hell", "are lobster"]\
        Tokens: ["What ", " ", "s"]
