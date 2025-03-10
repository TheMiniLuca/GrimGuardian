package com.gmail.theminiluca.grim.guardian.utils.config.model.tool;

import com.github.retrooper.packetevents.protocol.world.states.type.StateType;
import com.github.retrooper.packetevents.protocol.world.states.type.StateTypes;
import com.gmail.theminiluca.grim.guardian.GrimGuardian;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;

@Getter
public class StateSetTag extends HashSet<StateType> {


    private final String name;
    public static final Map<String, StateSetTag> STATE_MAP = new HashMap<>();

    public static final StateSetTag LEAVES = new StateSetTag("leaves",
            StateTypes.OAK_LEAVES,
            StateTypes.SPRUCE_LEAVES,
            StateTypes.BIRCH_LEAVES,
            StateTypes.JUNGLE_LEAVES,
            StateTypes.ACACIA_LEAVES,
            StateTypes.CHERRY_LEAVES,
            StateTypes.DARK_OAK_LEAVES,
            StateTypes.MANGROVE_LEAVES,
            StateTypes.AZALEA_LEAVES, StateTypes.FLOWERING_AZALEA_LEAVES);
    public static final StateSetTag WOOLS = new StateSetTag("wools",
            StateTypes.WHITE_WOOL,
            StateTypes.ORANGE_WOOL,
            StateTypes.MAGENTA_WOOL,
            StateTypes.LIGHT_BLUE_WOOL,
            StateTypes.YELLOW_WOOL,
            StateTypes.LIME_WOOL,
            StateTypes.PINK_WOOL,
            StateTypes.GRAY_WOOL,
            StateTypes.LIGHT_GRAY_WOOL,
            StateTypes.CYAN_WOOL,
            StateTypes.PURPLE_WOOL,
            StateTypes.BLUE_WOOL,
            StateTypes.BROWN_WOOL,
            StateTypes.GREEN_WOOL,
            StateTypes.RED_WOOL,
            StateTypes.BLACK_WOOL);

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
        put(LEAVES);
        put(WOOLS);

    }

    public StateSetTag(String name, StateType... stateTypes) {
        this.name = name;
        addAll(List.of(stateTypes));
    }


    public static @Nullable StateSetTag valueOf(String name) {
        return STATE_MAP.get(name);
    }
}
