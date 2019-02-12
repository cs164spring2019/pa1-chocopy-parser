# CS 164: Programming Assignment 1

[PA1 Specification]: http://inst.eecs.berkeley.edu/~cs164/sp19/hw/PA1.pdf
[ChocoPy Specification]: http://inst.eecs.berkeley.edu/~cs164/sp19/chocopy_language_reference.pdf

## Getting started

Run the following command to generate and compile your parser, and then run all the provided tests:

    mvn clean package

    java -cp "chocopy-ref.jar:target/assignment.jar" chocopy.ChocoPy --pass=s --test --dir src/test/data/pa1/sample/

In the starter code, only one test should pass. Your objective is to build a parser that passes all the provided tests and meets the assignment specifications.

To manually observe the output of your parser when run on a given input ChocoPy program, run the following command (replace the last argument to change the input file):

    java -cp "chocopy-ref.jar:target/assignment.jar" chocopy.ChocoPy --pass=s src/test/data/pa1/sample/expr_plus.py

You can check the output produced by the staff-provided reference implementation on the same input file, as follows:

    java -cp "chocopy-ref.jar:target/assignment.jar" chocopy.ChocoPy --pass=r src/test/data/pa1/sample/expr_plus.py

Try this with another input file as well, such as `src/test/data/pa1/sample/coverage.py`, to see what happens when the results disagree.

## Assignment specifications

See the [PA1 specification][] on the Lectures and Assignments page of our
website for a detailed specification of the assignment.
Refer to the [ChocoPy Specification][] on the CS164 web site
for the specification of the ChocoPy language. 

## Receiving updates to this repository

Add the `upstream` repository remotes (you only need to do this once in your local clone):

    git remote add upstream https://github.com/cs164spring2019/pa1-chocopy-parser.git

To sync with updates upstream:

    git pull upstream master


## Submission writeup

Team member 1: 

Team member 2: 

Team member 3: 

Team member 4: 

(Students should edit this section with their write-up)
