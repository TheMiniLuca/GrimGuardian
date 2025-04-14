package com.gmail.theminiluca.grim.guardian.utils.config.model.tool;

import com.github.retrooper.packetevents.protocol.world.states.defaulttags.BlockTags;
import com.github.retrooper.packetevents.protocol.world.states.type.StateType;
import com.github.retrooper.packetevents.protocol.world.states.type.StateTypes;
import com.gmail.theminiluca.grim.guardian.GrimGuardian;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;

@Getter
public class StateSetTag extends HashSet<StateType> {


    private final String name;
    public static final Map<String, StateSetTag> STATE_MAP = new HashMap<>();

    public static void put(StateSetTag setTag) {
        STATE_MAP.put(setTag.getName(), setTag);
    }
    static {
        try {
            for (Field field : StateTypes.class.getDeclaredFields()) {
                if (Modifier.isStatic(field.getModifiers()) &&
                        StateType.class.isAssignableFrom(field.getType())) {
                    StateType stat = (StateType) field.get(null);
                    STATE_MAP.put(stat.getName(), new StateSetTag(stat.getName(), stat));
                }
            }
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Failed to initialize ItemStat map", e);
        }

        try {
            for (Field field : BlockTags.class.getDeclaredFields()) {
                if (Modifier.isStatic(field.getModifiers()) &&
                        BlockTags.class.isAssignableFrom(field.getType())) {
                    BlockTags stat = (BlockTags) field.get(null);
                    STATE_MAP.put(stat.getName(), new StateSetTag(stat.getName(), stat.getStates()));
                }
            }
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Failed to initialize ItemStat map", e);
        }
    }

    public StateSetTag(String name, StateType... stateTypes) {
        this.name = name;
        addAll(List.of(stateTypes));
    }

    public StateSetTag(String name, Set<StateType> stateTypes) {
        this.name = name;
        addAll(stateTypes);
    }


    public static @Nullable StateSetTag valueOf(String name) {
        return STATE_MAP.get(name);
    }
}
