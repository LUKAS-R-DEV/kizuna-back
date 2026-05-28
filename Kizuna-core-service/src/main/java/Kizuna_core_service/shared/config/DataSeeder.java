package Kizuna_core_service.shared.config;

import Kizuna_core_service.inventory.domain.Inventory;
import Kizuna_core_service.inventory.domain.Status;
import Kizuna_core_service.inventory.domain.Type;
import Kizuna_core_service.inventory.repository.InventoryRepository;
import Kizuna_core_service.inventory_movement.domain.InventoryMovement;
import Kizuna_core_service.inventory_movement.domain.MovementType;
import Kizuna_core_service.inventory_movement.repository.InventoryMovementRepository;
import Kizuna_core_service.productionOrder.domain.ProductionOrder;
import Kizuna_core_service.productionOrder.domain.ProductionOrderStatus;
import Kizuna_core_service.productionOrder.repository.ProductionOrderRepository;
import Kizuna_core_service.qualityInspection.domain.QualityInspection;
import Kizuna_core_service.qualityInspection.domain.QualityInspectionStatus;
import Kizuna_core_service.qualityInspection.repository.QualityInspectionRepository;
import Kizuna_core_service.recipe.domain.Recipe;
import Kizuna_core_service.recipe.domain.RecipeItem;
import Kizuna_core_service.recipe.repository.RecipeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
@Profile("dev")
public class DataSeeder implements CommandLineRunner {

    private final InventoryRepository inventoryRepository;
    private final RecipeRepository recipeRepository;
    private final ProductionOrderRepository productionOrderRepository;
    private final QualityInspectionRepository qualityInspectionRepository;
    private final InventoryMovementRepository inventoryMovementRepository;

    @Override
    @Transactional
    public void run(String... args) {
        log.info("Starting data seeding...");

        // Clear existing data
        inventoryMovementRepository.deleteAll();
        qualityInspectionRepository.deleteAll();
        productionOrderRepository.deleteAll();
        recipeRepository.deleteAll();
        inventoryRepository.deleteAll();

        log.info("Cleared existing data");

        // Seed Inventory
        var rawMaterials = seedRawMaterials();
        var finishedProducts = seedFinishedProducts();
        log.info("Seeded {} inventory items", rawMaterials.size() + finishedProducts.size());

        // Seed Recipes
        var recipes = seedRecipes(rawMaterials, finishedProducts);
        log.info("Seeded {} recipes", recipes.size());

        // Seed Production Orders
        var orders = seedProductionOrders(recipes);
        log.info("Seeded {} production orders", orders.size());

        // Seed Quality Inspections
        var inspections = seedQualityInspections(orders);
        log.info("Seeded {} quality inspections", inspections.size());

        // Seed Inventory Movements
        var movements = seedInventoryMovements(rawMaterials);
        log.info("Seeded {} inventory movements", movements.size());

        log.info("Data seeding completed successfully!");
    }

    private java.util.List<Inventory> seedRawMaterials() {
        var materials = java.util.List.of(
                createInventory("Parafuso M6 Zincado", "Ferramentas", "Setor A-01", 1000.0, 100.0, Type.RAW, "Fornecedor A", Status.GOOD),
                createInventory("Porca M6", "Ferramentas", "Setor A-02", 1500.0, 150.0, Type.RAW, "Fornecedor A", Status.GOOD),
                createInventory("Arruela Lisa", "Ferramentas", "Setor A-03", 2000.0, 200.0, Type.RAW, "Fornecedor B", Status.GOOD),
                createInventory("Chapa de Aço 2mm", "Matéria-Prima", "Setor B-01", 500.0, 50.0, Type.RAW, "Aço Brasil", Status.GOOD),
                createInventory("Tubo Aluminio 1\"", "Matéria-Prima", "Setor B-02", 300.0, 30.0, Type.RAW, "Alumínio Nacional", Status.GOOD),
                createInventory("Cabo Elétrico 4mm", "Elétrica", "Setor C-01", 500.0, 50.0, Type.RAW, "Eletrica Plus", Status.GOOD),
                createInventory("Prego 17x27", "Ferramentas", "Setor A-04", 800.0, 80.0, Type.RAW, "Fornecedor A", Status.CRITICAL),
                createInventory("Tinta Prata", "Acabamento", "Setor D-01", 50.0, 10.0, Type.RAW, "Tintas Color", Status.GOOD)
        );
        return inventoryRepository.saveAll(materials);
    }

    private java.util.List<Inventory> seedFinishedProducts() {
        var products = java.util.List.of(
                createInventory("Estrutura Metálica Padrão", "Produto Final", "Depósito Central", 50.0, 5.0, Type.FINISHED, "Kizuna Production", Status.GOOD),
                createInventory("Painel Elétrico M", "Produto Final", "Depósito Central", 20.0, 3.0, Type.FINISHED, "Kizuna Production", Status.GOOD),
                createInventory("Suporte Ajustável", "Produto Final", "Depósito Central", 30.0, 5.0, Type.FINISHED, "Kizuna Production", Status.CRITICAL)
        );
        return inventoryRepository.saveAll(products);
    }

    private Inventory createInventory(String name, String category, String location, Double quantity, 
                                       Double minStock, Type type, String supplier, Status status) {
        Inventory inv = new Inventory();
        inv.setName(name);
        inv.setCategory(category);
        inv.setLocation(location);
        inv.setQuantity(quantity);
        inv.setMinStock(minStock);
        inv.setType(type);
        inv.setSupplier(supplier);
        inv.setStatus(status);
        inv.setActive(true);
        return inv;
    }

    private java.util.List<Recipe> seedRecipes(java.util.List<Inventory> rawMaterials, java.util.List<Inventory> finishedProducts) {
        var recipes = new java.util.ArrayList<Recipe>();

        // Recipe 1: Estrutura Metálica Padrão
        var recipe1 = new Recipe();
        recipe1.setName("Estrutura Metálica Padrão - Montagem");
        recipe1.setDescription("Processo de montagem da estrutura metálica padrão com soldagem e acabamento");
        recipe1.setActive(true);
        recipe1.setEstimatedProductionTime(3600L);
        recipe1.setProduct(finishedProducts.get(0));

        Set<RecipeItem> items1 = new HashSet<>();
        items1.add(createRecipeItem(recipe1, rawMaterials.get(3), 2.0)); // Chapa de Aço
        items1.add(createRecipeItem(recipe1, rawMaterials.get(0), 4.0));  // Parafuso M6
        items1.add(createRecipeItem(recipe1, rawMaterials.get(2), 4.0));  // Arruela
        recipe1.setItems(items1);
        recipes.add(recipeRepository.save(recipe1));

        // Recipe 2: Painel Elétrico M
        var recipe2 = new Recipe();
        recipe2.setName("Painel Elétrico M - Montagem");
        recipe2.setDescription("Montagem do painel elétrico médio com fiação e componentes");
        recipe2.setActive(true);
        recipe2.setEstimatedProductionTime(2400L);
        recipe2.setProduct(finishedProducts.get(1));

        Set<RecipeItem> items2 = new HashSet<>();
        items2.add(createRecipeItem(recipe2, rawMaterials.get(3), 1.0));  // Chapa de Aço
        items2.add(createRecipeItem(recipe2, rawMaterials.get(5), 5.0));  // Cabo Elétrico
        items2.add(createRecipeItem(recipe2, rawMaterials.get(4), 2.0));  // Tubo Alumínio
        recipe2.setItems(items2);
        recipes.add(recipeRepository.save(recipe2));

        // Recipe 3: Suporte Ajustável
        var recipe3 = new Recipe();
        recipe3.setName("Suporte Ajustável - Montagem");
        recipe3.setDescription("Montagem do suporte ajustável com regulagem de altura");
        recipe3.setActive(true);
        recipe3.setEstimatedProductionTime(1800L);
        recipe3.setProduct(finishedProducts.get(2));

        Set<RecipeItem> items3 = new HashSet<>();
        items3.add(createRecipeItem(recipe3, rawMaterials.get(4), 3.0));  // Tubo Alumínio
        items3.add(createRecipeItem(recipe3, rawMaterials.get(0), 2.0));   // Parafuso M6
        items3.add(createRecipeItem(recipe3, rawMaterials.get(1), 2.0));   // Porca M6
        recipe3.setItems(items3);
        recipes.add(recipeRepository.save(recipe3));

        return recipes;
    }

    private RecipeItem createRecipeItem(Recipe recipe, Inventory inventory, double quantity) {
        RecipeItem item = new RecipeItem();
        item.setRecipe(recipe);
        item.setInventory(inventory);
        item.setQuantity(quantity);
        return item;
    }

    private java.util.List<ProductionOrder> seedProductionOrders(java.util.List<Recipe> recipes) {
        var orders = new java.util.ArrayList<ProductionOrder>();
        String[] operators = {"user001", "user002", "user003"};
        String[] operatorNames = {"João Silva", "Maria Santos", "Pedro Costa"};

        // Ordem 1 - PLANNED
        var order1 = new ProductionOrder();
        order1.setRecipe(recipes.get(0));
        order1.setQuantityToProduce(10);
        order1.setPriority(1);
        order1.setOperatorId(operators[0]);
        order1.setOperatorName(operatorNames[0]);
        order1.setStatus(ProductionOrderStatus.PLANNED);
        order1.setQueuePosition(1);
        order1.setCreatedBy("admin");
        order1.setDeadline(LocalDateTime.now().plusDays(3));
        orders.add(productionOrderRepository.save(order1));

        // Ordem 2 - IN_PROGRESS
        var order2 = new ProductionOrder();
        order2.setRecipe(recipes.get(1));
        order2.setQuantityToProduce(5);
        order2.setPriority(2);
        order2.setOperatorId(operators[1]);
        order2.setOperatorName(operatorNames[1]);
        order2.setStatus(ProductionOrderStatus.IN_PROGRESS);
        order2.setQueuePosition(2);
        order2.setCreatedBy("admin");
        order2.setStartTime(LocalDateTime.now().minusHours(2));
        order2.setDeadline(LocalDateTime.now().plusDays(2));
        orders.add(productionOrderRepository.save(order2));

        // Ordem 3 - WAITING_INSPECTION
        var order3 = new ProductionOrder();
        order3.setRecipe(recipes.get(2));
        order3.setQuantityToProduce(8);
        order3.setPriority(3);
        order3.setOperatorId(operators[2]);
        order3.setOperatorName(operatorNames[2]);
        order3.setStatus(ProductionOrderStatus.WAITING_INSPECTION);
        order3.setQueuePosition(3);
        order3.setCreatedBy("admin");
        order3.setStartTime(LocalDateTime.now().minusHours(5));
        order3.setEndTime(LocalDateTime.now().minusMinutes(30));
        order3.setDeadline(LocalDateTime.now().plusDays(1));
        orders.add(productionOrderRepository.save(order3));

        // Ordem 4 - FINISHED_BY_TIME
        var order4 = new ProductionOrder();
        order4.setRecipe(recipes.get(0));
        order4.setQuantityToProduce(15);
        order4.setPriority(1);
        order4.setOperatorId(operators[0]);
        order4.setOperatorName(operatorNames[0]);
        order4.setStatus(ProductionOrderStatus.FINISHED_BY_TIME);
        order4.setQueuePosition(4);
        order4.setCreatedBy("admin");
        order4.setStartTime(LocalDateTime.now().minusDays(1));
        order4.setEndTime(LocalDateTime.now().minusHours(2));
        order4.setDeadline(LocalDateTime.now().minusDays(1));
        orders.add(productionOrderRepository.save(order4));

        // Ordem 5 - CANCELLED
        var order5 = new ProductionOrder();
        order5.setRecipe(recipes.get(1));
        order5.setQuantityToProduce(3);
        order5.setPriority(5);
        order5.setOperatorId(operators[1]);
        order5.setOperatorName(operatorNames[1]);
        order5.setStatus(ProductionOrderStatus.CANCELLED);
        order5.setQueuePosition(5);
        order5.setCreatedBy("admin");
        order5.setDeadline(LocalDateTime.now().plusDays(5));
        orders.add(productionOrderRepository.save(order5));

        return orders;
    }

    private java.util.List<QualityInspection> seedQualityInspections(java.util.List<ProductionOrder> orders) {
        var inspections = new java.util.ArrayList<QualityInspection>();

        // Inspeção 1 - APROVADA
        var inspection1 = new QualityInspection();
        inspection1.setProductionOrder(orders.get(2)); // Ordem WAITING_INSPECTION
        inspection1.setStatus(QualityInspectionStatus.APPROVED);
        inspection1.setNotes("Produto aprovado conforme especificações técnicas. Sem ressalvas.");
        inspection1.setInspectedBy("inspector001");
        inspections.add(qualityInspectionRepository.save(inspection1));

        // Inspeção 2 - REWORK
        var inspection2 = new QualityInspection();
        inspection2.setProductionOrder(orders.get(0));
        inspection2.setStatus(QualityInspectionStatus.REWORK);
        inspection2.setNotes("Solda inconsistente no ponto 3. Necessário retrabalho no setor de soldagem.");
        inspection2.setInspectedBy("inspector002");
        inspections.add(qualityInspectionRepository.save(inspection2));

        // Inspeção 3 - REJECTED
        var inspection3 = new QualityInspection();
        inspection3.setProductionOrder(orders.get(3));
        inspection3.setStatus(QualityInspectionStatus.REJECTED);
        inspection3.setNotes("Material com ranhura profunda não conforme com padrão de qualidade. Descartar.");
        inspection3.setInspectedBy("inspector001");
        inspections.add(qualityInspectionRepository.save(inspection3));

        return inspections;
    }

    private java.util.List<InventoryMovement> seedInventoryMovements(java.util.List<Inventory> rawMaterials) {
        var movements = new java.util.ArrayList<InventoryMovement>();

        // Entrada de estoque
        movements.add(createMovement(rawMaterials.get(0), 100.0, "Entrada de estoque inicial", MovementType.ENTRY));
        movements.add(createMovement(rawMaterials.get(3), 50.0, "Entrada de estoque inicial", MovementType.ENTRY));
        movements.add(createMovement(rawMaterials.get(5), 25.0, "Entrada de estoque inicial", MovementType.ENTRY));

        // Saída para produção
        movements.add(createMovement(rawMaterials.get(0), -20.0, "Consumo OP #001 - Estrutura Metálica", MovementType.ENTRY));
        movements.add(createMovement(rawMaterials.get(3), -10.0, "Consumo OP #001 - Estrutura Metálica", MovementType.ENTRY));
        movements.add(createMovement(rawMaterials.get(5), -15.0, "Consumo OP #002 - Painel Elétrico", MovementType.ENTRY));

        // Ajuste de estoque

        return inventoryMovementRepository.saveAll(movements);
    }

    private InventoryMovement createMovement(Inventory inventory, Double quantity, String reason, MovementType type) {
        InventoryMovement movement = new InventoryMovement();
        movement.setInventory(inventory);
        movement.setQuantity(quantity);
        movement.setReason(reason);
        movement.setType(type);
        return movement;
    }
}
