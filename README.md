Example run of Solver with default settings:

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

The java source implements a simple greedy best-first search that never backs up. 
It favors new moves over previously tried moves, and when there are no new moves left from a position, it chooses randomly.

For comparison, the Kotlin source implements A* to find optimal solutions.

```
[3, 4, 8]
[1, 6, 0]
[2, 7, 5]

hamming solution found in 19 ms:
	iterations: 4973
	pathLen: 21
euclidean solution found in 3 ms:
	iterations: 861
	pathLen: 21
manhattan solution found in 1 ms:
	iterations: 473
	pathLen: 21
```
