package com.gmail.theminiluca.grim.guardian.module;

import ac.grim.grimac.GrimAPI;
import ac.grim.grimac.player.GrimPlayer;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.attribute.Attribute;
import com.github.retrooper.packetevents.event.PacketListener;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.attribute.Attributes;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.player.User;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerUpdateAttributes;
import com.gmail.theminiluca.grim.guardian.GrimGuardian;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Iterator;

public class AttributeController implements PacketListener {

    @Override
    public void onPacketSend(PacketSendEvent event) {
        User user = event.getUser();
        if (user.getUUID() == null) return;
        Player player = Bukkit.getPlayer(user.getUUID());
        if (player == null) {
            return;
        }
        if (event.getPacketType().equals(PacketType.Play.Server.UPDATE_ATTRIBUTES)) {
            if (event.isCancelled()) {
                GrimGuardian.getInstance().getLogger().severe("I'm not sure which plugin is responsible, " +
                        "but canceling the event causes the Anti-cheat module to malfunction and not work properly.");
                GrimGuardian.getInstance().getLogger().severe("It was applied forcefully.");
            }
            WrapperPlayServerUpdateAttributes packet = new WrapperPlayServerUpdateAttributes(event);
            if (user.getEntityId() != packet.getEntityId()) return;
            int i = 0;
            Iterator<WrapperPlayServerUpdateAttributes.Property> iterator = packet.getProperties().iterator();
            while (iterator.hasNext()) {
                WrapperPlayServerUpdateAttributes.Property attributes = iterator.next();
                if (!(Attributes.PLAYER_BLOCK_BREAK_SPEED.equals(attributes.getAttribute()))) continue;
                GrimPlayer grimPlayer = GrimAPI.INSTANCE.getPlayerDataManager().getPlayer(player);
                if (grimPlayer.compensatedEntities.getSelf()
                        .getAttributeValue(ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.attribute.Attributes.getByName(attributes.getAttribute().getName().toString())) == 0.0F) {
                    iterator.remove();  // 안전하게 리스트에서 요소 제거
                    continue;
                }
                attributes.setValue(0.0F);
            }
            if (packet.getProperties().isEmpty()) {
                event.setCancelled(true);
            }
        }
    }

}
