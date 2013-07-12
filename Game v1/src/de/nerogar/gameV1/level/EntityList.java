package de.nerogar.gameV1.level;

import java.util.ArrayList;
import java.util.HashMap;

import de.nerogar.gameV1.Game;
import de.nerogar.gameV1.MathHelper;
import de.nerogar.gameV1.World;
import de.nerogar.gameV1.network.Packet;
import de.nerogar.gameV1.network.PacketEntity;
import de.nerogar.gameV1.physics.CollisionComparer;
import de.nerogar.gameV1.physics.Ray;

public class EntityList {
	public HashMap<Integer, Entity> entities = new HashMap<Integer, Entity>();
	public ArrayList<Entity> newEntities = new ArrayList<Entity>();
	private boolean updateInProgress = false;
	public int maxID;
	CollisionComparer collisionComparer;
	Game game;
	World world;

	public EntityList(Game game, World world) {
		this.game = game;
		this.world = world;
	}

	public void addEntity(Entity entity, World world) {
		if (!updateInProgress) {
			entities.put(entity.id, entity);
			entity.game = game;
			entity.init(world);
		} else {
			newEntities.add(entity);
			entity.world = world;
		}
	}

	private void addNewEntities() {
		for (int i = 0; i < newEntities.size(); i++) {
			addEntity(newEntities.get(i), newEntities.get(i).world);
		}

		newEntities = new ArrayList<Entity>();
	}

	public void removeNullEntities() {
		for (int i = entities.size() - 1; i >= 0; i--) {
			if (entities.get(i) == null || entities.get(i).markToRemove) {
				entities.remove(i);
			}
		}
	}

	public Entity[] getEntitiesInSight(Ray ray) {
		return collisionComparer.getEntitiesInRay(ray);
	}

	public void update(Game game, ArrayList<Packet> receivedPackets) {
		//for (int i = 0; i < entities.size(); i++) {
		//	entities.get(i).update(Timer.instance.delta / 1000F);
		//}
		removeNullEntities();
		updateInProgress = true;

		/*for (Entity e : entities) {
			e.update(game.timer.delta / 1000F);
		}*/
		
		for (Entity e: entities.values()) {
		    e.update(game.timer.delta / 1000F);
		}
		updateInProgress = false;
		addNewEntities();

		world.collisionComparer.updateGrid();
		world.collisionComparer.compare();
	}

	public void unloadAll() {
		entities.clear();
	}

	public void setCollisionComparer(CollisionComparer collisionComparer) {
		this.collisionComparer = collisionComparer;
	}

	public void render(Position loadPosition, int maxChunkRenderDistance) {
		for (int i = 0; i < entities.size(); i++) {
			Entity entity = entities.get(i);

			if (MathHelper.roundUpToInt(entity.matrix.position.getX(), Chunk.CHUNKSIZE) >= (loadPosition.x + 1) - maxChunkRenderDistance * Chunk.CHUNKSIZE && MathHelper.roundDownToInt(entity.matrix.position.getX(), Chunk.CHUNKSIZE) <= loadPosition.x + maxChunkRenderDistance * Chunk.CHUNKSIZE) {
				if (MathHelper.roundUpToInt(entity.matrix.position.getZ(), Chunk.CHUNKSIZE) >= (loadPosition.z + 1) - maxChunkRenderDistance * Chunk.CHUNKSIZE && MathHelper.roundDownToInt(entity.matrix.position.getZ(), Chunk.CHUNKSIZE) <= loadPosition.z + maxChunkRenderDistance * Chunk.CHUNKSIZE) {
					entity.render();
				}
			}
		}
	}
}
