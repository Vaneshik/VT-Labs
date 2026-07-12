package models;

import java.io.Serializable;

/**
 * Перечисление типов организаций.
 */
public enum OrganizationType implements Serializable {
    COMMERCIAL,
    GOVERNMENT,
    TRUST,
    OPEN_JOINT_STOCK_COMPANY;
}
