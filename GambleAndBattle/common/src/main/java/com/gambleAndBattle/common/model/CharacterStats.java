package com.gambleAndBattle.common.model;

/**
 * Représente les 5 statistiques principales d'un personnage dans GambleAndBattle.
 * Compatible avec la sérialisation KryoNet (nécessite un constructeur vide).
 */
public class CharacterStats {

    // 1. Points de Vie (PV)
    private int currentHp;
    private int maxHp;

    // 2. Résistance Magique (RM)
    private int magicResistance;

    // 3. Résistance Physique (RP)
    private int physicalResistance;

    // 4. Mana (Ressource pour les sorts)
    private int currentMana;
    private int maxMana;

    // 5. Endurance (Ressource pour le déplacement / actions de combat)
    private int currentStamina;
    private int maxStamina;

    /**
     * Constructeur par défaut obligatoire pour la sérialisation Kryo.
     */
    public CharacterStats() {}

    /**
     * Constructeur d'initialisation complet.
     */
    public CharacterStats(int maxHp, int magicResistance, int physicalResistance, int maxMana, int maxStamina) {
        this.maxHp = maxHp;
        this.currentHp = maxHp;
        this.magicResistance = magicResistance;
        this.physicalResistance = physicalResistance;
        this.maxMana = maxMana;
        this.currentMana = maxMana;
        this.maxStamina = maxStamina;
        this.currentStamina = maxStamina;
    }

    // ==========================================
    // MÉTHODES MÉTIER (Gestion des ressources)
    // ==========================================

    /**
     * Applique des dégâts physiques réduits par la Résistance Physique (RP).
     */
    public void takePhysicalDamage(int rawDamage) {
        int netDamage = Math.max(1, rawDamage - physicalResistance);
        this.currentHp = Math.max(0, this.currentHp - netDamage);
    }

    /**
     * Applique des dégâts magiques réduits par la Résistance Magique (RM).
     */
    public void takeMagicDamage(int rawDamage) {
        int netDamage = Math.max(1, rawDamage - magicResistance);
        this.currentHp = Math.max(0, this.currentHp - netDamage);
    }

    /**
     * Soigne le personnage sans dépasser son maxHp.
     */
    public void heal(int amount) {
        this.currentHp = Math.min(this.maxHp, this.currentHp + amount);
    }

    /**
     * Tente de consommer du mana. Renvoie true si l'action est possible.
     */
    public boolean consumeMana(int amount) {
        if (this.currentMana >= amount) {
            this.currentMana -= amount;
            return true;
        }
        return false;
    }

    /**
     * Tente de consommer de l'endurance. Renvoie true si l'action est possible.
     */
    public boolean consumeStamina(int amount) {
        if (this.currentStamina >= amount) {
            this.currentStamina -= amount;
            return true;
        }
        return false;
    }

    /**
     * Régénère le mana sans dépasser le maximum.
     */
    public void restoreMana(int amount) {
        this.currentMana = Math.min(this.maxMana, this.currentMana + amount);
    }

    /**
     * Régénère l'endurance sans dépasser le maximum.
     */
    public void restoreStamina(int amount) {
        this.currentStamina = Math.min(this.maxStamina, this.currentStamina + amount);
    }

    public boolean isDead() {
        return this.currentHp <= 0;
    }

    // ==========================================
    // GETTERS & SETTERS
    // ==========================================

    public int getCurrentHp() { return currentHp; }
    public void setCurrentHp(int currentHp) { this.currentHp = Math.min(maxHp, Math.max(0, currentHp)); }

    public int getMaxHp() { return maxHp; }
    public void setMaxHp(int maxHp) { this.maxHp = maxHp; }

    public int getMagicResistance() { return magicResistance; }
    public void setMagicResistance(int magicResistance) { this.magicResistance = magicResistance; }

    public int getPhysicalResistance() { return physicalResistance; }
    public void setPhysicalResistance(int physicalResistance) { this.physicalResistance = physicalResistance; }

    public int getCurrentMana() { return currentMana; }
    public void setCurrentMana(int currentMana) { this.currentMana = Math.min(maxMana, Math.max(0, currentMana)); }

    public int getMaxMana() { return maxMana; }
    public void setMaxMana(int maxMana) { this.maxMana = maxMana; }

    public int getCurrentStamina() { return currentStamina; }
    public void setCurrentStamina(int currentStamina) { this.currentStamina = Math.min(maxStamina, Math.max(0, currentStamina)); }

    public int getMaxStamina() { return maxStamina; }
    public void setMaxStamina(int maxStamina) { this.maxStamina = maxStamina; }
}