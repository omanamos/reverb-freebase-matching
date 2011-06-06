Reverb Freebase Matching System
-------------------------------
For running instructions, run the application with the -h flag.

Resources Needed
----------------------
./weights.config -> contains weights for scoring - see example in project directory
./word_weights.config -> contains word counts based off of 9m ReVerb corpus - see example in project directory
./stop_words.config -> contains words not to be matched on -> see example in project directory
freebase_file -> contains freebase entities to match to, should be ordered by prominence -> see example at data/output.fbid-prominence.sorted
wiki aliases file -> contains aliases to match to -> see example at data/output.wiki-aliases.sorted
index directory -> contains Lucene index for spellchecking -> use index directory in project directory

Where Stuff is Located
----------------------

Scoring Algorithm
-----------------
src/wrappers/Result.java -> function: computeScores(Entity e, Double factor) -> handles how the scores are combined
src/wrappers/Score.java -> handles how the scores are combined
src/wrappers/Weights.java -> stores the matching weights after loading them from weights.config

Matching Algorithm
------------------
src/matching/Freebase.java -> handles all matching, passes matches to Result.java for scoring

Accuracy Evaluation
-------------------
src/analysis/Analyze.java -> processes output
src/AccMeasurements.java -> computes accuracies

Other Important
---------------
src/matching/Main.java -> Main program
src/wrappers/Options.java -> handles argument processing
src/labeling/Labeler.java -> takes the same arguments as the main program. Does a randomly picked match, and then asks if it is a good match. Processes 100 ReVerb matches and then exits. Outputs to ./labels.output
src/matching/Matcher.java -> prompts user for search queries and prints out top 10 results. Useful for debugging. Takes the same args as the main program.

