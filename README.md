The Java source implements a simple greedy best-first search that never backs up. It favors new moves over previously 
tried moves, and when there are no new moves left from a position, it chooses randomly.

```
     0 1 2
    ------
 0|  1 2 3
 1|  4 5 6
 2|  7 8 0

runs: 10
heuristic: avg moves
	manhattan: 250
	euclidean: 436
	hamming: 1446
	random: 294180
```


For comparison, the Kotlin source implements A* to find optimal solutions. You can tweak the weight parameter in the 
source to find bounded solutions to larger problems.

```
[7, 0, 3]
[1, 4, 2]
[5, 8, 6]

manhattan solution found in 53 ms
	iterations: 104
	pathLen: 15
euclidean solution found in 2 ms
	iterations: 114
	pathLen: 15
hamming solution found in 3 ms
	iterations: 414
	pathLen: 15
```

The output between the two implementations
isn't consistent because they were written independently and are posted here together for educational purposes. If I 
find the time, I'll come back and normalize the outputs later.

The `Grapher.kt` file contains an ad hoc program that generates the state space graph up to a certain depth limit.
Take a look in the [`img`](https://github.com/entangledloops/SlidingTilePuzzle/blob/master/img) subdirectory for some 
full resolution outputs. (Note: Github doesn't render large images well in your browser. You'll probably need to
download the files and view them on your machine.) It's very much a hack-job; you'll need to have LaTeX and Graphviz 
installed, and may need to be running Linux to use it. Here's a low-res example of the output:

![dot-2048](https://github.com/entangledloops/SlidingTilePuzzle/blob/master/img/dot-2048-small.png)
