
# FuzzyRegex


## Fuzzy Regex Pattern Matching and Capturing


----


### Table of Contents:

- [**Examples**](#examples)
    - [Standard](#standard)
    - [Ambiguous](#ambiguous)

+ [**Usage**](#usage)
    + [Methods](#methods)
    + [Parameters](#parameters)
    + [Overloads](#overloads)


----


# Examples


## Standard

Match strings to patterns and extract variables even if the input text does not match the pattern exactly:

```java
Pattern: "my name is ¿ and I am ¿ years old"

Input: "my name is John and I am 30 years old"
    Score: 1.0
    Variables: ["John", "30"]
    Tokens: ["my name is ", " and I am ", " years old"]

Input: "My names John and I'm 30 years old."
    Score: 0.8285714285714286
    Variables: ["John", "30"]
    Tokens: ["My names ", " and I'm ", " years old."]
```


## Ambiguous

In ambiguous cases all valid extraction results are returned:

```java
Pattern: "What ¿ ¿s"

Input: "What the hell are lobsters"
    Score: 1.0
    
    Extraction 1:
        Variables: ["the", "hell are lobster"]
        Tokens: ["What ", " ", "s"]
    
    Extraction 2:
        Variables: ["the hell are", "lobster"]
        Tokens: ["What ", " ", "s"]
    
    Extraction 3:
        Variables: ["the hell", "are lobster"]
        Tokens: ["What ", " ", "s"]
```


&nbsp;

----


# Usage


## Methods

There are two methods available:

- **stringCompare()** - Determines how closely an input string matches a pattern and returns a value between 0 and 1
- **stringEditDistance()** - Determines how closely an input string matches a pattern and returns the number of edits required on the input string in order for it to match the pattern


## Parameters

Both [methods](#methods) can take the same parameters:

| **PARAMETER**           | **TYPE**               | **DESCRIPTION**                                                                                   |
|:------------------------|:-----------------------|:--------------------------------------------------------------------------------------------------|
|                         |                        |                                                                                                   |
| **_pattern_**           | String                 | The pattern to compare against (the wildcard symbol is `¿`)                                       |
| **_text_**              | String                 | The input string to compare to the **_pattern_**                                                  |
| **_vars_**              | List\<List\<String\>\> | If included, this list will be populated with the extracted variables found during the comparison |
| **_tokens_**            | List\<List\<String\>\> | If included, this list will be populated with the extracted tokens found during the comparison    |
| **_ignoreCase_**        | boolean                | When true, the comparison is performed without case sensativity _(false by default)_              |
| **_ignorePunctuation_** | boolean                | When true, punctation is ignored during the comparison _(false by default)_                       |
|                         |                        |                                                                                                   |


## Overloads

Both [methods](#methods) have the same overloads using the [parameters](#parameters) defined above:

| **_pattern_** | **_text_** | **_vars_** | **_tokens_** | **_ignoreCase_** | **_ignorePunctuation_** |
|:-------------:|:----------:|:----------:|:------------:|:----------------:|:-----------------------:|
|      `X`      |    `X`     |            |              |                  |                         |
|      `X`      |    `X`     |            |              |       `X`        |                         |
|      `X`      |    `X`     |            |              |       `X`        |           `X`           |
|      `X`      |    `X`     |    `X`     |     `X`      |                  |                         |
|      `X`      |    `X`     |    `X`     |     `X`      |       `X`        |                         |
|      `X`      |    `X`     |    `X`     |     `X`      |       `X`        |           `X`           |


&nbsp;

----
