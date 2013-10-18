package de.nerogar.gameV1.network;

import de.nerogar.gameV1.Vector3d;
import de.nerogar.gameV1.DNFileSystem.DNFile;
import de.nerogar.gameV1.physics.ObjectMatrix;

public class PacketMoveEntity extends PacketEntity {

	public ObjectMatrix objectMatrix;
	public boolean includeScale = false;

	public PacketMoveEntity() {
		channel = ENTITY_CHANNEL;
	}

	@Override
	public void pack() {
		double[] omParts = new double[includeScale ? 9 : 6];

		omParts[0] = objectMatrix.position.getX();
		omParts[1] = objectMatrix.position.getY();
		omParts[2] = objectMatrix.position.getZ();

		omParts[3] = objectMatrix.rotation.getX();
		omParts[4] = objectMatrix.rotation.getY();
		omParts[5] = objectMatrix.rotation.getZ();

		if (includeScale) {
			omParts[6] = objectMatrix.scaling.getX();
			omParts[7] = objectMatrix.scaling.getY();
			omParts[8] = objectMatrix.scaling.getZ();
		}

		data = new DNFile("");
		data.addNode("om", omParts);
		data.addNode("id", entityID);

		packedData = data.toByteArray();

	}

	@Override
	public void unpack() {
		data = new DNFile("");
		data.fromByteArray(packedData);

		double[] omParts = data.getDoubleArray("om");

		Vector3d pos = new Vector3d(omParts[0], omParts[1], omParts[2]);
		Vector3d rot = new Vector3d(omParts[3], omParts[4], omParts[5]);
		Vector3d scal;
		if (includeScale) {
			scal = new Vector3d(omParts[6], omParts[7], omParts[8]);
		} else {
			scal = new Vector3d();
		}

		objectMatrix = new ObjectMatrix(pos, rot, scal);
		entityID = data.getInt("id");
	}

	@Override
	public String getName() {
		return "moveEntity";
	}
}