package de.nerogar.gameV1.ai;

import de.nerogar.gameV1.level.Chunk;
import de.nerogar.gameV1.level.Land;
import de.nerogar.gameV1.level.Position;

public class Pathfinder {

	private Land land;
	private static final int MAX_NODE_SIZE = 8;

	public Pathfinder(Land land) {
		this.land = land;
	}

	public void updateNodeMap(Chunk chunk) { //fertig
		boolean[][] walkableMap = chunk.walkableMap;

		//long time1 = System.nanoTime(); //time1

		PathNode[][] nodeMap = new PathNode[walkableMap.length][walkableMap[0].length];
		chunk.nodeMap = nodeMap;
		for (int i = 0; i < walkableMap.length; i++) {
			for (int j = 0; j < walkableMap.length; j++) {
				PathNode newNode = new PathNode(i, j, chunk);
				newNode.walkable = walkableMap[i][j];
				nodeMap[i][j] = newNode;
			}
		}

		//long time2 = System.nanoTime(); //time2

		int iteration = 2;
		int iterationHalf = 1;
		while (iteration <= walkableMap.length && iteration <= MAX_NODE_SIZE) {

			for (int i = 0; i < walkableMap.length; i += iteration) {
				for (int j = 0; j < walkableMap.length; j += iteration) {

					boolean m00 = nodeMap[i][j].mergeable;
					boolean m01 = nodeMap[i][j + (iterationHalf)].mergeable;
					boolean m10 = nodeMap[i + (iterationHalf)][j].mergeable;
					boolean m11 = nodeMap[i + (iterationHalf)][j + (iterationHalf)].mergeable;

					if (m00 && m01 && m10 && m11) {
						//calc node-connection
						boolean w00 = nodeMap[i][j].walkable;
						boolean w01 = nodeMap[i][j + (iterationHalf)].walkable;
						boolean w10 = nodeMap[i + (iterationHalf)][j].walkable;
						boolean w11 = nodeMap[i + (iterationHalf)][j + (iterationHalf)].walkable;

						if (w00 == w01 && w01 == w10 && w10 == w11) {
							PathNode newNode = new PathNode(i, j, chunk);
							newNode.size = iteration;
							newNode.mergeable = true;
							newNode.walkable = w00;

							for (int i2 = 0; i2 < iteration; i2++) {
								for (int j2 = 0; j2 < iteration; j2++) {
									nodeMap[i + i2][j + j2] = newNode;
								}
							}

						} else {
							nodeMap[i][j].mergeable = false;
							nodeMap[i][j + (iterationHalf)].mergeable = false;
							nodeMap[i + (iterationHalf)][j].mergeable = false;
							nodeMap[i + (iterationHalf)][j + (iterationHalf)].mergeable = false;
						}
					} else {
						for (int i2 = 0; i2 < iteration; i2++) {
							for (int j2 = 0; j2 < iteration; j2++) {
								nodeMap[i + i2][j + j2].mergeable = false;
							}
						}
					}
				}
			}

			iteration *= 2;
			iterationHalf *= 2;
		}

		//long time3 = System.nanoTime(); //time3

		//wird jetzt in land gemacht
		/*for (int i = chunk.chunkPosition.x - 1; i <= chunk.chunkPosition.x + 1; i++) {
			for (int j = chunk.chunkPosition.z - 1; j <= chunk.chunkPosition.z + 1; j++) {
				//System.out.println("update neighbors of (" + i + "|" + j + ")");
				updateNodeNeighbors(land.getChunk(new Position(i, j)));
			}
		}
		updateNodeNeighbors(chunk);*/

		//long time4 = System.nanoTime(); //time4

		//System.out.println("--1: " + (time2 - time1) / 1000000D + " ms");
		//System.out.println("--2: " + (time3 - time2) / 1000000D + " ms");
		//System.out.println("--3: " + (time4 - time3) / 1000000D + " ms");
	}

	public void updateNodeNeighbors(Chunk chunk) {
		if (chunk == null) return;

		PathNode[][] nodeMap = chunk.nodeMap;
		for (int i = 0; i < nodeMap.length; i++) {
			for (int j = 0; j < nodeMap[i].length; j = nodeMap[i][j].locZ + nodeMap[i][j].size) {
				//System.out.println(nodeMap[i][j].locZ);

				if (!nodeMap[i][j].neighborsProcessed) {
					PathNode node = nodeMap[i][j];

					int x0 = node.x - 1;
					int x1 = node.x + node.size;
					int y0 = node.z - 1;
					int y1 = node.z + node.size;

					/*x0 = x0 < 0 ? 0 : x0;
					x1 = x1 < nodeMap.length ? x1 : nodeMap.length - 1;
					y0 = y0 < 0 ? 0 : y0;
					y1 = y1 < nodeMap.length ? y1 : nodeMap.length - 1;*/

					if (y0 != node.z) {
						//System.out.println("step1");
						//System.out.println(x0 + " - " + x1);
						PathNode tempNode;
						for (int i1 = x0; i1 <= x1; i1 = tempNode.x + tempNode.size) {
							//System.out.println(i1);
							Chunk tempChunk;
							if (x0 >= chunk.chunkPosition.x * Chunk.CHUNKSIZE && x1 < nodeMap.length + chunk.chunkPosition.x * Chunk.CHUNKSIZE && y0 >= chunk.chunkPosition.z * Chunk.CHUNKSIZE && y1 < nodeMap.length + chunk.chunkPosition.z * Chunk.CHUNKSIZE) {
								tempChunk = chunk;
							} else {
								tempChunk = land.getChunk(land.getChunkPosition(i1, y0));
							}
							if (tempChunk != null) {
								tempNode = tempChunk.getPathNode(i1, y0);
								if (isWalkable(node, tempNode)) {
									node.addNeighbor(tempNode);
								}
							} else {
								tempNode = new PathNode(0, 0, chunk);
								tempNode.x = i1;
								tempNode.z = y0;

								tempNode.size = 1;
							}

						}
					}

					if (y1 != node.z + node.size - 1) {
						//System.out.println("step2");
						//System.out.println(x0 + " - " + x1);
						PathNode tempNode;
						for (int i1 = x0; i1 <= x1; i1 = tempNode.x + tempNode.size) {
							Chunk tempChunk;
							if (x0 >= chunk.chunkPosition.x * Chunk.CHUNKSIZE && x1 < nodeMap.length + chunk.chunkPosition.x * Chunk.CHUNKSIZE && y0 >= chunk.chunkPosition.z * Chunk.CHUNKSIZE && y1 < nodeMap.length + chunk.chunkPosition.z * Chunk.CHUNKSIZE) {

								tempChunk = chunk;
							} else {
								tempChunk = land.getChunk(land.getChunkPosition(i1, y1));
							}
							if (tempChunk != null) {
								tempNode = tempChunk.getPathNode(i1, y1);
								if (isWalkable(node, tempNode)) {
									node.addNeighbor(tempNode);
								}
							} else {
								tempNode = new PathNode(0, 0, chunk);
								tempNode.x = i1;
								tempNode.z = y1;

								tempNode.size = 1;
							}
						}
					}

					if (x0 != node.x) {
						//System.out.println("step3");
						//System.out.println((y0 + 1) + " - " + y1);
						PathNode tempNode;
						for (int i1 = y0 + 1; i1 < y1; i1 = tempNode.z + tempNode.size) {
							Chunk tempChunk;
							if (x0 >= chunk.chunkPosition.x * Chunk.CHUNKSIZE && x1 < nodeMap.length + chunk.chunkPosition.x * Chunk.CHUNKSIZE && y0 >= chunk.chunkPosition.z * Chunk.CHUNKSIZE && y1 < nodeMap.length + chunk.chunkPosition.z * Chunk.CHUNKSIZE) {
								tempChunk = chunk;
							} else {
								tempChunk = land.getChunk(land.getChunkPosition(x0, i1));
							}
							if (tempChunk != null) {
								tempNode = tempChunk.getPathNode(x0, i1);
								if (isWalkable(node, tempNode)) {
									node.addNeighbor(tempNode);
								}
							} else {
								tempNode = new PathNode(0, 0, chunk);
								tempNode.x = x0;
								tempNode.z = i1;

								tempNode.size = 1;
							}
						}
					}

					if (x1 != node.x + node.size - 1) {
						//System.out.println("step4");
						//System.out.println((y0 + 1) + " - " + y1);
						PathNode tempNode;
						for (int i1 = y0 + 1; i1 < y1; i1 = tempNode.z + tempNode.size) {
							//System.out.println(i1);
							Chunk tempChunk;
							if (x0 >= chunk.chunkPosition.x * Chunk.CHUNKSIZE && x1 < nodeMap.length + chunk.chunkPosition.x * Chunk.CHUNKSIZE && y0 >= chunk.chunkPosition.z * Chunk.CHUNKSIZE && y1 < nodeMap.length + chunk.chunkPosition.z * Chunk.CHUNKSIZE) {
								tempChunk = chunk;
							} else {
								tempChunk = land.getChunk(land.getChunkPosition(x1, i1));
							}
							if (tempChunk != null) {
								tempNode = tempChunk.getPathNode(x1, i1);
								if (isWalkable(node, tempNode)) {
									node.addNeighbor(tempNode);
								}
							} else {
								tempNode = new PathNode(0, 0, chunk);
								tempNode.x = x1;
								tempNode.z = i1;

								tempNode.size = 1;
							}
						}
					}

					nodeMap[i][j].neighborsProcessed = true;
					node.finalizeNeighbors();
				}
			}
		}

		for (int i = 0; i < nodeMap.length; i++) {
			for (int j = 0; j < nodeMap[i].length; j = nodeMap[i][j].locZ + nodeMap[i][j].size) {
				nodeMap[i][j].neighborsProcessed = false;
			}
		}
	}

	public boolean isWalkable(PathNode source, PathNode target) {

		if (!(source.walkable && target.walkable)) return false;

		if (target.x >= source.x && target.x < source.x + source.size) {
			return true;
		} else if (target.z >= source.z && target.z < source.z + source.size) {
			return true;
		} else {
			for (int i = (source.x < target.x ? source.x : target.x); i <= (source.x < target.x ? target.x : source.x); i++) {
				Chunk tempChunk;
				int nodeSize = 1;
				for (int j = (source.z < target.z ? source.z : target.z); j <= (source.z < target.z ? target.z : source.z); j += nodeSize) {
					Position posInChunk = land.getPositionInChunk(i, j);
					tempChunk = land.getChunk(land.getChunkPosition(i, j));
					if (tempChunk != null) {
						nodeSize = tempChunk.nodeMap[posInChunk.x][posInChunk.z].size; //gibt manchmal NullPointerExceptions (beobachten)
						if (!tempChunk.walkableMap[posInChunk.x][posInChunk.z]) { return false; }
					}
				}
			}
		}

		return true;
	}

	public PathNode getNode(Position position) {
		Chunk tempChunk = land.getChunk(land.getChunkPosition(position));
		Position posInChunk = land.getPositionInChunk(position);
		return tempChunk.nodeMap[posInChunk.x][posInChunk.z];
	}
}