package ru.bclib.api.dataexchange.handler;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import ru.bclib.BCLib;
import ru.bclib.api.dataexchange.DataHandler;
import ru.bclib.api.dataexchange.DataHandlerDescriptor;
import ru.bclib.gui.screens.ConfirmRestartScreen;
import ru.bclib.gui.screens.SyncFilesScreen;
import ru.bclib.util.Pair;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class SendFiles extends DataHandler {
	public static DataHandlerDescriptor DESCRIPTOR = new DataHandlerDescriptor(new ResourceLocation(BCLib.MOD_ID, "send_files"), SendFiles::new, false, false);

	protected List<DataExchange.AutoFileSyncEntry> files;
	private String token = "";
	public SendFiles(){
		this(null, "");
	}
	public SendFiles(List<DataExchange.AutoFileSyncEntry> files, String token) {
		super(DESCRIPTOR.IDENTIFIER, true);
		this.files = files;
		this.token = token;
	}
	
	@Override
	protected void serializeData(FriendlyByteBuf buf) {
		List<DataExchange.AutoFileSyncEntry> existingFiles = files.stream().filter(e -> e.fileName.exists()).collect(Collectors.toList());
		writeString(buf, token);
		buf.writeInt(existingFiles.size());

		BCLib.LOGGER.info("Sending " + existingFiles.size() + " Files to Client:");
		for (DataExchange.AutoFileSyncEntry entry : existingFiles) {
			int length = entry.serializeContent(buf);
			BCLib.LOGGER.info("    - " + entry + " (" + length + " Bytes)");
		}
	}

	private List<Pair<DataExchange.AutoFileSyncEntry, byte[]>> receivedFiles;
	@Override
	protected void deserializeFromIncomingData(FriendlyByteBuf buf, PacketSender responseSender, boolean fromClient) {
		token = readString(buf);
		if (!token.equals(RequestFiles.currentToken)) {
			BCLib.LOGGER.error("Unrequested File Transfer!");
			receivedFiles = new ArrayList<>(0);
			return;
		}

		int size = buf.readInt();
		receivedFiles = new ArrayList<>(size);
		BCLib.LOGGER.info("Server sent " + size + " Files:");
		for (int i=0; i<size; i++){
			Pair<DataExchange.AutoFileSyncEntry, byte[]> p = DataExchange.AutoFileSyncEntry.deserializeContent(buf);
			if (p.first != null) {
				receivedFiles.add(p);
				BCLib.LOGGER.info("    - " + p.first + " (" + p.second.length + " Bytes)");
			} else {
				BCLib.LOGGER.error("    - Failed to receive File");
			}
		}
	}
	
	@Override
	protected void runOnGameThread(Minecraft client, MinecraftServer server, boolean isClient) {
		BCLib.LOGGER.info("Writing Files:");
		for (Pair<DataExchange.AutoFileSyncEntry, byte[]> entry : receivedFiles) {
			final DataExchange.AutoFileSyncEntry e = entry.first;
			final byte[] data = entry.second;
			Path path = e.fileName.toPath();
			BCLib.LOGGER.info("    - Writing " + path + " (" + data.length + " Bytes)");
			try {
				Files.write(path, data);
			} catch (IOException ioException) {
				BCLib.LOGGER.error("    --> Writing "+e.fileName+" failed: " + ioException);
			}
		}

		showConfirmRestart(client);
	}

	@Environment(EnvType.CLIENT)
	protected void showConfirmRestart(Minecraft client){
		client.setScreen(new ConfirmRestartScreen(() -> {
			Minecraft.getInstance().setScreen((Screen)null);
			client.stop();
		}));

	}
}
