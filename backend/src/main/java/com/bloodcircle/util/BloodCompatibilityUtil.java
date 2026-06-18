package com.bloodcircle.util;

import java.util.*;

/**
 * Blood compatibility mapping — identical to Flask BLOOD_COMPATIBILITY dict.
 * Maps donor blood group → list of blood groups they can donate to.
 */
public final class BloodCompatibilityUtil {

    private BloodCompatibilityUtil() {}

    /**
     * Donor → can donate to these recipients
     */
    public static final Map<String, List<String>> BLOOD_COMPATIBILITY;

    /**
     * Recipient → can receive from these donors
     */
    public static final Map<String, List<String>> RECEIVE_FROM;

    static {
        Map<String, List<String>> compat = new LinkedHashMap<>();
        compat.put("O-",  List.of("O-", "O+", "A-", "A+", "B-", "B+", "AB-", "AB+"));
        compat.put("O+",  List.of("O+", "A+", "B+", "AB+"));
        compat.put("A-",  List.of("A-", "A+", "AB-", "AB+"));
        compat.put("A+",  List.of("A+", "AB+"));
        compat.put("B-",  List.of("B-", "B+", "AB-", "AB+"));
        compat.put("B+",  List.of("B+", "AB+"));
        compat.put("AB-", List.of("AB-", "AB+"));
        compat.put("AB+", List.of("AB+"));
        BLOOD_COMPATIBILITY = Collections.unmodifiableMap(compat);

        Map<String, List<String>> recv = new LinkedHashMap<>();
        recv.put("O-",  List.of("O-"));
        recv.put("O+",  List.of("O-", "O+"));
        recv.put("A-",  List.of("A-", "O-"));
        recv.put("A+",  List.of("A-", "A+", "O-", "O+"));
        recv.put("B-",  List.of("B-", "O-"));
        recv.put("B+",  List.of("B-", "B+", "O-", "O+"));
        recv.put("AB-", List.of("AB-", "A-", "B-", "O-"));
        recv.put("AB+", List.of("O-", "O+", "A-", "A+", "B-", "B+", "AB-", "AB+"));
        RECEIVE_FROM = Collections.unmodifiableMap(recv);
    }

    /**
     * Given a required blood group (recipient), find all donor blood groups that can donate to them.
     * Identical to Flask get_compatible_blood_groups().
     */
    public static List<String> getCompatibleDonorGroups(String bloodGroupRequired) {
        List<String> compatible = new ArrayList<>();
        for (Map.Entry<String, List<String>> entry : BLOOD_COMPATIBILITY.entrySet()) {
            if (entry.getValue().contains(bloodGroupRequired)) {
                compatible.add(entry.getKey());
            }
        }
        return compatible;
    }
}
