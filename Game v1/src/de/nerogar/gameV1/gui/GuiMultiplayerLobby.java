package de.nerogar.gameV1.gui;

import java.util.ArrayList;
import java.util.HashMap;

import de.nerogar.gameV1.Game;
import de.nerogar.gameV1.RenderHelper;
import de.nerogar.gameV1.internalServer.Faction;
import de.nerogar.gameV1.internalServer.InternalServer;
import de.nerogar.gameV1.network.*;

public class GuiMultiplayerLobby extends Gui {
	private GElementButton startButton, backButton;
	private GElementButton readyButton, kickButton;
	private GElementListBox playersList;
	private HashMap<Client, Boolean> readyStates;
	private boolean readyState = false;
	private Server server;
	private Client client;

	public GuiMultiplayerLobby(Game game, Server server, Client client) {
		super(game);
		this.server = server;
		this.client = client;
		readyStates = new HashMap<Client, Boolean>();
		PacketMultiplayerLobbyClient lobbyInfoClient = new PacketMultiplayerLobbyClient();
		lobbyInfoClient.readyState = false;
		client.sendPacket(lobbyInfoClient);

		aktivateStartGameButton();
	}

	@Override
	public boolean pauseGame() {
		return true;
	}

	@Override
	public String getName() {
		return "createWorld";
	}

	@Override
	public void init() {
		//addGElement(new GElementTextLabel(genNewID(), 0.0f, 0.05f, 0.4f, 0.1f, "Multiplayer", FontRenderer.CENTERED));
		setTitel("Multiplayer Lobby");

		playersList = new GElementListBox(genNewID(), 0.5f, 0.2f, 0.5f, 0.4f, new String[] {}, "buttons/button.png", "buttons/scrollbar.png");
		playersList.sliderWidth = 0.02f;

		kickButton = new GElementButton(genNewID(), 0.1f, 0.5f, 0.2f, 0.1f, "kick", FontRenderer.CENTERED, "buttons/button.png", false, "");

		addGElement(new GElementTextLabel(genNewID(), 0.5f, 0.7f, 0.1f, 0.1f, "ready:", FontRenderer.RIGHT));
		readyButton = new GElementButton(genNewID(), 0.6f, 0.7f, 0.05f, 0.1f, "ready", FontRenderer.CENTERED, "buttons/button.png", true, "buttons/tick.png");

		startButton = new GElementButton(genNewID(), 0.1f, 0.7f, 0.35f, 0.1f, "start Game", FontRenderer.CENTERED, "buttons/button.png", false, "");

		backButton = new GElementButton(genNewID(), 0.1f, 0.85f, 0.3f, 0.1f, "back", FontRenderer.CENTERED, "buttons/button.png", false, "");

		addGElement(playersList);
		addGElement(kickButton);
		addGElement(readyButton);
		addGElement(backButton);
	}

	public void aktivateStartGameButton() {
		if (server != null) addGElement(startButton);
	}

	@Override
	public void updateGui() {
		//update player List as server
		if (server != null) {
			ArrayList<Client> clients = server.getClients();
			//String[] clientNames = new String[clients.size()];
			PacketMultiplayerLobbyInfo packetLobbyInfo = new PacketMultiplayerLobbyInfo();

			packetLobbyInfo.playerNames = new String[clients.size()];
			packetLobbyInfo.playerReadyStates = new boolean[clients.size()];

			for (int i = 0; i < clients.size(); i++) {
				if (clients.get(i).connectionInfo != null) {
					Client connectionClient = clients.get(i);
					ArrayList<Packet> receivedPackets = connectionClient.getData(Packet.LOBBY_CHANNEL);
					if (receivedPackets != null) {
						for (Packet packet : receivedPackets) {
							processServerPackets(connectionClient, packet);
						}
					}
				}
			}
			//playersList.text = clientNames;
			//server.broadcastData(packetLobbyInfo);
		}

		//update packets
		ArrayList<Packet> receivedPackets = client.getData(Packet.LOBBY_CHANNEL);
		if (receivedPackets != null) {
			processClientPackets(receivedPackets);
		}

		//process connection reset
		if (!client.connected) {
			disconnect();
			game.guiList.alert(new Alert(game, client.closeMessage));
		}

		//update ready state button
		if (readyState) {
			readyButton.overlayImage = "buttons/tick.png";
		} else {
			readyButton.overlayImage = "buttons/cross.png";
		}
	}

	public void processClientPackets(ArrayList<Packet> receivedPackets) {
		for (Packet packet : receivedPackets) {
			if (packet instanceof PacketMultiplayerLobbyInfo) {
				PacketMultiplayerLobbyInfo lobbyInfo = (PacketMultiplayerLobbyInfo) packet;
				String[] clientNames = lobbyInfo.playerNames;
				if (clientNames != null) {
					playersList.text = clientNames;
				}
			} else if (packet instanceof PacketExitMultiplayerLobby) {
				PacketExitMultiplayerLobby exitPacket = (PacketExitMultiplayerLobby) packet;
				game.guiList.removeGui(getName());
				game.world.initiateClientWorld(client, Faction.getClientFaction(exitPacket.factionID), Faction.getClientFactions(exitPacket.factionIDs));
			}
		}
	}

	public void processServerPackets(Client connectionClient, Packet packet) {
		if (packet instanceof PacketMultiplayerLobbyClient) {
			PacketMultiplayerLobbyClient lobbyInfoClient = (PacketMultiplayerLobbyClient) packet;
			readyStates.put(client, lobbyInfoClient.readyState);

			PacketMultiplayerLobbyInfo packetLobbyInfo = new PacketMultiplayerLobbyInfo();
			ArrayList<Client> clients = server.getClients();
			packetLobbyInfo.playerNames = new String[clients.size()];
			packetLobbyInfo.playerReadyStates = new boolean[clients.size()];

			for (int i = 0; i < clients.size(); i++) {
				if (clients.get(i).connectionInfo != null) {
					Client serverClient = clients.get(i);
					packetLobbyInfo.playerNames[i] = serverClient.connectionInfo.username;
					//packetLobbyInfo.playerReadyStates[i] = readyStates.get(serverClient);
				}
			}

			server.broadcastData(packetLobbyInfo);
		}
	}

	public void disconnect() {
		if (server != null) {
			server.stopServer();
		}
		client.stopClient();
		game.guiList.removeGui(getName());
		game.guiList.addGui(new GuiMultiplayer(game));
	}

	@Override
	public void render() {
		RenderHelper.renderDefaultGuiBackground();
		renderGui();
	}

	@Override
	public void clickButton(int id, int mouseButton) {
		if (id == backButton.id && mouseButton == 0) {
			disconnect();
		} else if (id == readyButton.id && mouseButton == 0) {
			readyState = !readyState;
		} else if (id == startButton.id && mouseButton == 0) {
			InternalServer internalServer = new InternalServer(game, server);
			game.internalServer = internalServer;

			Faction[] playingFactions = new Faction[Faction.getMaxFactionCount() < server.getClients().size() ? Faction.getMaxFactionCount() : server.getClients().size()];
			int[] playingFactionIDs = new int[playingFactions.length];
			for (int i = 0; i < playingFactions.length; i++) {
				playingFactions[i] = Faction.getServerFaction(i);
				playingFactionIDs[i] = playingFactions[i].id;
			}

			for (int i = 0; i < playingFactions.length; i++) {
				PacketExitMultiplayerLobby exitPacket = new PacketExitMultiplayerLobby();
				exitPacket.factionID = i;
				exitPacket.factionIDs = playingFactionIDs;
				server.getClients().get(i).sendPacket(exitPacket);
			}

			internalServer.initiateWorld("serverWorld", 0, playingFactions); //TODO hardcoded world for now
		}
	}
}