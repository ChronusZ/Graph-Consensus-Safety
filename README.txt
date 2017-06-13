A graph consensus safety testing demo by Ethan MacBrough.

A precompiled .JAR file can be found in /out/artifacts/graphConsensus_jar/. You might need to installed the latest Java SE to run it.

Drag the screen to move the viewport around. Click the left mouse button on a node to select it as the target for testing. Right click on a node to toggle its status as an "infected"
node. To compute the "corruptability of information" from the other nodes, click on the "Compute Safety" button. How corrupted the information from a given node is expected to be is
reflected in the color of that node: green signifies the information is uncorruptable, red signifies the information is corruptable. Blue signifies that the node has no path of
communication with the target. You can also hover over the node to show the exact amount of corruptability.

To create new vertices, hold shift and left click somewhere on the background. To remove a vertex, hold shift and right click on it. To add an edge, hold shift and left click on the
"tail" and then hold shift and left click on the "head".

The corruptability formula is

C = \frac{\sum_{p\in P_S} l(p)^{-\alpha}}{\sum_{p\in P} l(p)^{-\alpha}}

where P is the set of minimal paths from the node to the target, P_S is the set of minimal paths from the node to the target passing through an infected node, l(p) is the length of the
path p (i.e., the number of edges in it), and \alpha is the "length effect" which can be changed using the slider in the top right. Higher length effect values mean that longer paths
are counted as less important, since shorter paths are less likely to be tampered with. Here a minimal path means a path p which contains no duplicate vertices and for which there
exists no path p' such that the set of vertices in p contains every vertex in p'. It makes sense to only consider minimal paths because non-minimal paths only given more chance to
be tampered with.