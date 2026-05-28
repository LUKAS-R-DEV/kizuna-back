package Kizuna_core_service.shared.messaging;

import lombok.NoArgsConstructor;


public final class EventTopics {

    private EventTopics() {
    }

    public static final String PRODUCTION_ORDER = "event.production_order";
    public static final String INVENTORY = "event.inventory";
    public static final String INVENTORY_DISABLE = "event.inventory.disabled";
    public static final String INVENTORY_ENTRY = "event.inventory.entry";
    public static final String INVENTORY_EXIT = "event.inventory.exit";
    public static final String INVENTORY_MOVEMENT = "event.inventory_movement";
    public static final String QUALITY_INSPECTION = "event.quality_inspection";
    public static final String AUDIT = "event.audit";
    public static final String NOTIFICATION = "event.notification";

    public static final String PRODUCTION_CREATED = "event.production.created";
    public static final String PRODUCTION_STARTED = "event.production.started";
    public static final String PRODUCTION_FINISHED = "event.production.finished";
    public static final String PRODUCTION_REWORK = "event.production.rework";
    public static final String PRODUCTION_PAUSED = "event.production.paused";

    public static final String INVENTORY_CREATED = "event.inventory.created";
    public static final String INVENTORY_UPDATED = "event.inventory.updated";


    public static final String RECIPE_CREATED = "event.recipe.created";
    public static final String RECIPE_UPDATED = "event.recipe.updated";
    public static final String RECIPE_DISABLED = "event.recipe.disabled";

    public static final String INSPECTION_APPROVED = "event.inspection.approved";
    public static final String INSPECTION_REJECTED = "event.inspection.rejected";
    public static final String INSPECTION_REWORK = "event.inspection.rework";








}
