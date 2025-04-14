//package com.hamderber.chunklibrary.util;
//
//import com.hamderber.chunklibrary.ChunkLibrary;
//import com.hamderber.chunklibrary.events.ChunkEvent;
//
//import net.neoforged.bus.api.SubscribeEvent;
//
//public class EventAnnouncer {
//	@SubscribeEvent
//	private void onReadStart(ChunkEvent.StartLoad event) {
//		ChunkLibrary.LOGGER.debug("Start load event for chunk at " + event.pos.toString());
//	}
//	
//	@SubscribeEvent
//	private void onReadEnd(ChunkEvent.EndLoad event) {
//		ChunkLibrary.LOGGER.debug("End load event for chunk at " + event.pos.toString());
//	}
//}
