-- ============================================
-- Kizuna Core Service - Seed Data
-- ============================================

-- Clear existing data (optional - use with caution)
-- TRUNCATE TABLE quality_inspection, production_order, recipe_item, recipe, inventory_movements, inventory CASCADE;

-- ============================================
-- INVENTORY (Raw Materials & Products)
-- ============================================

-- Raw Materials
INSERT INTO inventory (name, category, location, quantity, min_stock, supplier, status, type, active) VALUES
('Aço Carbono 1010', 'MATÉRIA-PRIMA', 'Setor A - Prateleira 1', 5000.0, 1000.0, 'Aço Brasil Ltda', 'GOOD', 'RAW', true),
('Alumínio 6061-T6', 'MATÉRIA-PRIMA', 'Setor A - Prateleira 2', 2500.0, 500.0, 'Alumínio Nacional', 'GOOD', 'RAW', true),
('Parafuso M8x25', 'COMPONENTES', 'Setor B - Caixa 3', 10000.0, 2000.0, 'Fasteners Ind', 'GOOD', 'RAW', true),
('Rolamento 6204', 'COMPONENTES', 'Setor B - Caixa 1', 500.0, 100.0, 'SKF Brasil', 'GOOD', 'RAW', true),
('Borracha Nitrílica', 'MATÉRIA-PRIMA', 'Setor C - Estante 2', 800.0, 200.0, 'Borrachas Sul', 'GOOD', 'RAW', true),
('Cobre Eletrolítico', 'MATÉRIA-PRIMA', 'Setor A - Prateleira 3', 1200.0, 300.0, 'Cobre Mineração', 'GOOD', 'RAW', true),
('Plástico ABS', 'MATÉRIA-PRIMA', 'Setor C - Estante 1', 1500.0, 400.0, 'Plastibras', 'GOOD', 'RAW', true),
('Circuito PCB-001', 'COMPONENTES', 'Setor D - Armário 1', 200.0, 50.0, 'Eletrônica Ltda', 'CRITICAL', 'RAW', true),
('Pintura Epóxi Branca', 'INSUMOS', 'Setor E - Depósito', 100.0, 20.0, 'Tintas Industriais', 'GOOD', 'RAW', true),
('Óleo Lubrificante', 'INSUMOS', 'Setor E - Depósito', 50.0, 10.0, 'Lubrificantes BR', 'GOOD', 'RAW', true);

-- Finished Products
INSERT INTO inventory (name, category, location, quantity, min_stock, supplier, status, type, active) VALUES
('Eixo Motor Industrial EM-100', 'PRODUTO ACABADO', 'Setor F - Expedição', 45.0, 20.0, NULL, 'GOOD', 'FINISHED', true),
('Eixo Motor Industrial EM-200', 'PRODUTO ACABADO', 'Setor F - Expedição', 30.0, 15.0, NULL, 'GOOD', 'FINISHED', true),
('Suporte Rotativo SR-500', 'PRODUTO ACABADO', 'Setor F - Expedição', 60.0, 25.0, NULL, 'GOOD', 'FINISHED', true),
('Caixa de Engrenagens CE-1000', 'PRODUTO ACABADO', 'Setor F - Expedição', 25.0, 10.0, NULL, 'GOOD', 'FINISHED', true),
('Painel de Controle PC-01', 'PRODUTO ACABADO', 'Setor G - Estoque Final', 15.0, 8.0, NULL, 'GOOD', 'FINISHED', true),
('Sensor de Temperatura ST-50', 'PRODUTO ACABADO', 'Setor G - Estoque Final', 80.0, 30.0, NULL, 'GOOD', 'FINISHED', true),
('Conjunto Mancal CM-250', 'PRODUTO ACABADO', 'Setor F - Expedição', 40.0, 20.0, NULL, 'GOOD', 'FINISHED', true),
('Eixo de Transmissão ET-400', 'PRODUTO ACABADO', 'Setor F - Expedição', 20.0, 12.0, NULL, 'CRITICAL', 'FINISHED', true);

-- ============================================
-- RECIPES (Bill of Materials)
-- ============================================

-- Get product IDs for recipes (will be auto-generated starting from last ID)
-- Recipe 1: Eixo Motor Industrial EM-100
INSERT INTO recipe (name, description, active, created_at, updated_at, estimated_production_time, product_id) VALUES
('Eixo Motor EM-100', 'Eixo de alta precisão para motores industriais de 10HP', true, NOW(), NOW(), 120, (SELECT id FROM inventory WHERE name = 'Eixo Motor Industrial EM-100'));

-- Recipe 2: Eixo Motor Industrial EM-200
INSERT INTO recipe (name, description, active, created_at, updated_at, estimated_production_time, product_id) VALUES
('Eixo Motor EM-200', 'Eixo de alta precisão para motores industriais de 20HP', true, NOW(), NOW(), 180, (SELECT id FROM inventory WHERE name = 'Eixo Motor Industrial EM-200'));

-- Recipe 3: Suporte Rotativo SR-500
INSERT INTO recipe (name, description, active, created_at, updated_at, estimated_production_time, product_id) VALUES
('Suporte Rotativo SR-500', 'Suporte completo para rotação de 500kg', true, NOW(), NOW(), 240, (SELECT id FROM inventory WHERE name = 'Suporte Rotativo SR-500'));

-- Recipe 4: Caixa de Engrenagens CE-1000
INSERT INTO recipe (name, description, active, created_at, updated_at, estimated_production_time, product_id) VALUES
('Caixa de Engrenagens CE-1000', 'Caixa redução 1000:1 com rolamentos de precisão', true, NOW(), NOW(), 360, (SELECT id FROM inventory WHERE name = 'Caixa de Engrenagens CE-1000'));

-- Recipe 5: Painel de Controle PC-01
INSERT INTO recipe (name, description, active, created_at, updated_at, estimated_production_time, product_id) VALUES
('Painel de Controle PC-01', 'Painel eletrônico para controle de processos', true, NOW(), NOW(), 90, (SELECT id FROM inventory WHERE name = 'Painel de Controle PC-01'));

-- Recipe 6: Sensor de Temperatura ST-50
INSERT INTO recipe (name, description, active, created_at, updated_at, estimated_production_time, product_id) VALUES
('Sensor de Temperatura ST-50', 'Sensor industrial de temperatura -50 a 200°C', true, NOW(), NOW(), 60, (SELECT id FROM inventory WHERE name = 'Sensor de Temperatura ST-50'));

-- Recipe 7: Conjunto Mancal CM-250
INSERT INTO recipe (name, description, active, created_at, updated_at, estimated_production_time, product_id) VALUES
('Conjunto Mancal CM-250', 'Mancal completo com rolamento e lubrificação', true, NOW(), NOW(), 150, (SELECT id FROM inventory WHERE name = 'Conjunto Mancal CM-250'));

-- ============================================
-- RECIPE ITEMS (Bill of Materials Details)
-- ============================================

-- Recipe 1 items: Eixo Motor EM-100
INSERT INTO recipe_item (recipe_id, inventory_id, quantity) VALUES
((SELECT id FROM recipe WHERE name = 'Eixo Motor EM-100'), (SELECT id FROM inventory WHERE name = 'Aço Carbono 1010'), 15.5),
((SELECT id FROM recipe WHERE name = 'Eixo Motor EM-100'), (SELECT id FROM inventory WHERE name = 'Rolamento 6204'), 2),
((SELECT id FROM recipe WHERE name = 'Eixo Motor EM-100'), (SELECT id FROM inventory WHERE name = 'Parafuso M8x25'), 8),
((SELECT id FROM recipe WHERE name = 'Eixo Motor EM-100'), (SELECT id FROM inventory WHERE name = 'Pintura Epóxi Branca'), 0.5);

-- Recipe 2 items: Eixo Motor EM-200
INSERT INTO recipe_item (recipe_id, inventory_id, quantity) VALUES
((SELECT id FROM recipe WHERE name = 'Eixo Motor EM-200'), (SELECT id FROM inventory WHERE name = 'Aço Carbono 1010'), 28.0),
((SELECT id FROM recipe WHERE name = 'Eixo Motor EM-200'), (SELECT id FROM inventory WHERE name = 'Rolamento 6204'), 4),
((SELECT id FROM recipe WHERE name = 'Eixo Motor EM-200'), (SELECT id FROM inventory WHERE name = 'Parafuso M8x25'), 12),
((SELECT id FROM recipe WHERE name = 'Eixo Motor EM-200'), (SELECT id FROM inventory WHERE name = 'Pintura Epóxi Branca'), 0.8);

-- Recipe 3 items: Suporte Rotativo SR-500
INSERT INTO recipe_item (recipe_id, inventory_id, quantity) VALUES
((SELECT id FROM recipe WHERE name = 'Suporte Rotativo SR-500'), (SELECT id FROM inventory WHERE name = 'Aço Carbono 1010'), 45.0),
((SELECT id FROM recipe WHERE name = 'Suporte Rotativo SR-500'), (SELECT id FROM inventory WHERE name = 'Rolamento 6204'), 6),
((SELECT id FROM recipe WHERE name = 'Suporte Rotativo SR-500'), (SELECT id FROM inventory WHERE name = 'Parafuso M8x25'), 24),
((SELECT id FROM recipe WHERE name = 'Suporte Rotativo SR-500'), (SELECT id FROM inventory WHERE name = 'Pintura Epóxi Branca'), 1.2);

-- Recipe 4 items: Caixa de Engrenagens CE-1000
INSERT INTO recipe_item (recipe_id, inventory_id, quantity) VALUES
((SELECT id FROM recipe WHERE name = 'Caixa de Engrenagens CE-1000'), (SELECT id FROM inventory WHERE name = 'Aço Carbono 1010'), 35.0),
((SELECT id FROM recipe WHERE name = 'Caixa de Engrenagens CE-1000'), (SELECT id FROM inventory WHERE name = 'Alumínio 6061-T6'), 12.0),
((SELECT id FROM recipe WHERE name = 'Caixa de Engrenagens CE-1000'), (SELECT id FROM inventory WHERE name = 'Rolamento 6204'), 8),
((SELECT id FROM recipe WHERE name = 'Caixa de Engrenagens CE-1000'), (SELECT id FROM inventory WHERE name = 'Parafuso M8x25'), 16),
((SELECT id FROM recipe WHERE name = 'Caixa de Engrenagens CE-1000'), (SELECT id FROM inventory WHERE name = 'Óleo Lubrificante'), 2.0);

-- Recipe 5 items: Painel de Controle PC-01
INSERT INTO recipe_item (recipe_id, inventory_id, quantity) VALUES
((SELECT id FROM recipe WHERE name = 'Painel de Controle PC-01'), (SELECT id FROM inventory WHERE name = 'Alumínio 6061-T6'), 3.5),
((SELECT id FROM recipe WHERE name = 'Painel de Controle PC-01'), (SELECT id FROM inventory WHERE name = 'Plástico ABS'), 2.0),
((SELECT id FROM recipe WHERE name = 'Painel de Controle PC-01'), (SELECT id FROM inventory WHERE name = 'Circuito PCB-001'), 1),
((SELECT id FROM recipe WHERE name = 'Painel de Controle PC-01'), (SELECT id FROM inventory WHERE name = 'Parafuso M8x25'), 6);

-- Recipe 6 items: Sensor de Temperatura ST-50
INSERT INTO recipe_item (recipe_id, inventory_id, quantity) VALUES
((SELECT id FROM recipe WHERE name = 'Sensor de Temperatura ST-50'), (SELECT id FROM inventory WHERE name = 'Cobre Eletrolítico'), 0.8),
((SELECT id FROM recipe WHERE name = 'Sensor de Temperatura ST-50'), (SELECT id FROM inventory WHERE name = 'Plástico ABS'), 0.5),
((SELECT id FROM recipe WHERE name = 'Sensor de Temperatura ST-50'), (SELECT id FROM inventory WHERE name = 'Circuito PCB-001'), 1),
((SELECT id FROM recipe WHERE name = 'Sensor de Temperatura ST-50'), (SELECT id FROM inventory WHERE name = 'Borracha Nitrílica'), 0.2);

-- Recipe 7 items: Conjunto Mancal CM-250
INSERT INTO recipe_item (recipe_id, inventory_id, quantity) VALUES
((SELECT id FROM recipe WHERE name = 'Conjunto Mancal CM-250'), (SELECT id FROM inventory WHERE name = 'Aço Carbono 1010'), 8.5),
((SELECT id FROM recipe WHERE name = 'Conjunto Mancal CM-250'), (SELECT id FROM inventory WHERE name = 'Rolamento 6204'), 2),
((SELECT id FROM recipe WHERE name = 'Conjunto Mancal CM-250'), (SELECT id FROM inventory WHERE name = 'Parafuso M8x25'), 4),
((SELECT id FROM recipe WHERE name = 'Conjunto Mancal CM-250'), (SELECT id FROM inventory WHERE name = 'Óleo Lubrificante'), 0.5),
((SELECT id FROM recipe WHERE name = 'Conjunto Mancal CM-250'), (SELECT id FROM inventory WHERE name = 'Pintura Epóxi Branca'), 0.3);

-- ============================================
-- PRODUCTION ORDERS
-- ============================================

-- Planned orders
INSERT INTO production_order (quantity_to_produce, start_time, estimated_total_time, operator_name, operator_id, end_time, rework_count, created_by, created_at, updated_at, priority, deadline, queue_position, status, recipe_id) VALUES
(50, NULL, 120, NULL, NULL, NULL, 0, 'admin', NOW(), NOW(), 1, NOW() + INTERVAL '7 days', 1, 'PLANNED', (SELECT id FROM recipe WHERE name = 'Eixo Motor EM-100')),
(30, NULL, 180, NULL, NULL, NULL, 0, 'admin', NOW(), NOW(), 2, NOW() + INTERVAL '10 days', 2, 'PLANNED', (SELECT id FROM recipe WHERE name = 'Eixo Motor EM-200')),
(40, NULL, 240, NULL, NULL, NULL, 0, 'admin', NOW(), NOW(), 3, NOW() + INTERVAL '5 days', 3, 'PLANNED', (SELECT id FROM recipe WHERE name = 'Suporte Rotativo SR-500'));

-- In progress orders
INSERT INTO production_order (quantity_to_produce, start_time, estimated_total_time, operator_name, operator_id, end_time, rework_count, created_by, created_at, updated_at, priority, deadline, queue_position, status, recipe_id) VALUES
(25, NOW() - INTERVAL '2 hours', 120, 'João Silva', 'OP001', NULL, 0, 'admin', NOW() - INTERVAL '1 day', NOW(), 1, NOW() + INTERVAL '3 days', NULL, 'IN_PROGRESS', (SELECT id FROM recipe WHERE name = 'Eixo Motor EM-100')),
(15, NOW() - INTERVAL '4 hours', 360, 'Maria Santos', 'OP002', NULL, 0, 'admin', NOW() - INTERVAL '2 days', NOW(), 2, NOW() + INTERVAL '5 days', NULL, 'IN_PROGRESS', (SELECT id FROM recipe WHERE name = 'Caixa de Engrenagens CE-1000'));

-- Waiting inspection orders
INSERT INTO production_order (quantity_to_produce, start_time, estimated_total_time, operator_name, operator_id, end_time, rework_count, created_by, created_at, updated_at, priority, deadline, queue_position, status, recipe_id) VALUES
(20, NOW() - INTERVAL '1 day', 90, 'Pedro Costa', 'OP003', NOW() - INTERVAL '30 minutes', 0, 'admin', NOW() - INTERVAL '3 days', NOW(), 1, NOW() + INTERVAL '2 days', NULL, 'WAITING_INSPECTION', (SELECT id FROM recipe WHERE name = 'Painel de Controle PC-01')),
(100, NOW() - INTERVAL '12 hours', 60, 'Ana Oliveira', 'OP004', NOW() - INTERVAL '1 hour', 0, 'admin', NOW() - INTERVAL '2 days', NOW(), 3, NOW() + INTERVAL '4 days', NULL, 'WAITING_INSPECTION', (SELECT id FROM recipe WHERE name = 'Sensor de Temperatura ST-50'));

-- Approved orders
INSERT INTO production_order (quantity_to_produce, start_time, estimated_total_time, operator_name, operator_id, end_time, rework_count, created_by, created_at, updated_at, priority, deadline, queue_position, status, recipe_id) VALUES
(60, NOW() - INTERVAL '3 days', 150, 'Carlos Lima', 'OP005', NOW() - INTERVAL '2 days', 0, 'admin', NOW() - INTERVAL '5 days', NOW(), 2, NOW() + INTERVAL '1 day', NULL, 'APPROVED', (SELECT id FROM recipe WHERE name = 'Conjunto Mancal CM-250'));

-- Rework orders
INSERT INTO production_order (quantity_to_produce, start_time, estimated_total_time, operator_name, operator_id, end_time, rework_count, created_by, created_at, updated_at, priority, deadline, queue_position, status, recipe_id) VALUES
(10, NOW() - INTERVAL '2 days', 120, 'João Silva', 'OP001', NOW() - INTERVAL '1 day', 1, 'admin', NOW() - INTERVAL '4 days', NOW(), 1, NOW() + INTERVAL '1 day', NULL, 'REWORK', (SELECT id FROM recipe WHERE name = 'Eixo Motor EM-200'));

-- Finished orders
INSERT INTO production_order (quantity_to_produce, start_time, estimated_total_time, operator_name, operator_id, end_time, rework_count, created_by, created_at, updated_at, priority, deadline, queue_position, status, recipe_id) VALUES
(35, NOW() - INTERVAL '5 days', 120, 'Maria Santos', 'OP002', NOW() - INTERVAL '4 days', 0, 'admin', NOW() - INTERVAL '7 days', NOW(), 2, NOW() - INTERVAL '2 days', NULL, 'FINISHED_BY_TIME', (SELECT id FROM recipe WHERE name = 'Eixo Motor EM-100')),
(45, NOW() - INTERVAL '6 days', 240, 'Pedro Costa', 'OP003', NOW() - INTERVAL '5 days', 0, 'admin', NOW() - INTERVAL '8 days', NOW(), 3, NOW() - INTERVAL '3 days', NULL, 'FINISHED_BY_TIME', (SELECT id FROM recipe WHERE name = 'Suporte Rotativo SR-500'));

-- Cancelled orders
INSERT INTO production_order (quantity_to_produce, start_time, estimated_total_time, operator_name, operator_id, end_time, rework_count, created_by, created_at, updated_at, priority, deadline, queue_position, status, recipe_id) VALUES
(20, NULL, 360, NULL, NULL, NULL, 0, 'admin', NOW() - INTERVAL '10 days', NOW(), 5, NOW() + INTERVAL '5 days', NULL, 'CANCELLED', (SELECT id FROM recipe WHERE name = 'Caixa de Engrenagens CE-1000'));

-- Rejected orders (for waste tracking)
INSERT INTO production_order (quantity_to_produce, start_time, estimated_total_time, operator_name, operator_id, end_time, rework_count, created_by, created_at, updated_at, priority, deadline, queue_position, status, recipe_id) VALUES
(15, NOW() - INTERVAL '4 days', 90, 'Ana Oliveira', 'OP004', NOW() - INTERVAL '3 days', 2, 'admin', NOW() - INTERVAL '6 days', NOW(), 4, NOW() + INTERVAL '2 days', NULL, 'REJECTED', (SELECT id FROM recipe WHERE name = 'Sensor de Temperatura ST-50'));

-- ============================================
-- QUALITY INSPECTIONS
-- ============================================

-- Approved inspections
INSERT INTO quality_inspection (production_order_id, status, notes, inspected_by, created_at) VALUES
((SELECT id FROM production_order WHERE status = 'APPROVED' LIMIT 1), 'APPROVED', 'Inspeção visual e dimensional aprovada. Tolerâncias dentro da especificação.', 'inspector', NOW() - INTERVAL '2 days'),
((SELECT id FROM production_order WHERE status = 'FINISHED_BY_TIME' LIMIT 1), 'APPROVED', 'Qualidade excelente, sem defeitos identificados.', 'inspector', NOW() - INTERVAL '4 days');

-- Rework inspections
INSERT INTO quality_inspection (production_order_id, status, notes, inspected_by, created_at) VALUES
((SELECT id FROM production_order WHERE status = 'REWORK' LIMIT 1), 'REWORK', 'Dimensões fora da tolerância em 0.05mm. Necessário retrabalho no acabamento.', 'inspector', NOW() - INTERVAL '1 day');

-- Rejected inspections
INSERT INTO quality_inspection (production_order_id, status, notes, inspected_by, created_at) VALUES
((SELECT id FROM production_order WHERE status = 'REJECTED' LIMIT 1), 'REJECTED', 'Falha crítica na soldagem. Produto fora da especificação e sem possibilidade de retrabalho.', 'inspector', NOW() - INTERVAL '3 days');

-- Waiting inspection entries
INSERT INTO quality_inspection (production_order_id, status, notes, inspected_by, created_at) VALUES
((SELECT id FROM production_order WHERE status = 'WAITING_INSPECTION' LIMIT 1), 'APPROVED', 'Pendente de inspeção detalhada.', 'inspector', NOW() - INTERVAL '30 minutes');

-- ============================================
-- INVENTORY MOVEMENTS
-- ============================================

-- Raw material entries (purchases)
INSERT INTO inventory_movements (inventory_id, quantity, reason, created_at, updated_at, type) VALUES
((SELECT id FROM inventory WHERE name = 'Aço Carbono 1010'), 2000.0, 'Entrada de compra - Nota Fiscal 12345', NOW() - INTERVAL '7 days', NOW() - INTERVAL '7 days', 'ENTRY'),
((SELECT id FROM inventory WHERE name = 'Alumínio 6061-T6'), 1000.0, 'Entrada de compra - Nota Fiscal 12346', NOW() - INTERVAL '5 days', NOW() - INTERVAL '5 days', 'ENTRY'),
((SELECT id FROM inventory WHERE name = 'Rolamento 6204'), 200.0, 'Entrada de compra - Nota Fiscal 12347', NOW() - INTERVAL '3 days', NOW() - INTERVAL '3 days', 'ENTRY'),
((SELECT id FROM inventory WHERE name = 'Parafuso M8x25'), 5000.0, 'Entrada de compra - Nota Fiscal 12348', NOW() - INTERVAL '2 days', NOW() - INTERVAL '2 days', 'ENTRY');

-- Production exits (raw materials consumed)
INSERT INTO inventory_movements (inventory_id, quantity, reason, created_at, updated_at, type) VALUES
((SELECT id FROM inventory WHERE name = 'Aço Carbono 1010'), -387.5, 'Consumo produção - Eixos EM-100 (25 un x 15.5kg)', NOW() - INTERVAL '2 days', NOW() - INTERVAL '2 days', 'EXIT'),
((SELECT id FROM inventory WHERE name = 'Aço Carbono 1010'), -420.0, 'Consumo produção - Eixos EM-200 (15 un x 28kg)', NOW() - INTERVAL '4 hours', NOW() - INTERVAL '4 hours', 'EXIT'),
((SELECT id FROM inventory WHERE name = 'Rolamento 6204'), -50, 'Consumo produção - Eixos EM-100 (25 un x 2 rol)', NOW() - INTERVAL '2 days', NOW() - INTERVAL '2 days', 'EXIT'),
((SELECT id FROM inventory WHERE name = 'Rolamento 6204'), -60, 'Consumo produção - Suporte SR-500 (10 un x 6 rol)', NOW() - INTERVAL '5 days', NOW() - INTERVAL '5 days', 'EXIT');

-- Production entries (finished products)
INSERT INTO inventory_movements (inventory_id, quantity, reason, created_at, updated_at, type) VALUES
((SELECT id FROM inventory WHERE name = 'Eixo Motor Industrial EM-100'), 25.0, 'Produção aprovada - Ordem #1', NOW() - INTERVAL '2 days', NOW() - INTERVAL '2 days', 'ENTRY'),
((SELECT id FROM inventory WHERE name = 'Conjunto Mancal CM-250'), 60.0, 'Produção aprovada - Ordem #7', NOW() - INTERVAL '2 days', NOW() - INTERVAL '2 days', 'ENTRY'),
((SELECT id FROM inventory WHERE name = 'Eixo Motor Industrial EM-100'), 35.0, 'Produção finalizada - Ordem histórica', NOW() - INTERVAL '4 days', NOW() - INTERVAL '4 days', 'ENTRY');

-- Waste entries (rejected production)
INSERT INTO inventory_movements (inventory_id, quantity, reason, created_at, updated_at, type) VALUES
((SELECT id FROM inventory WHERE name = 'Sensor de Temperatura ST-50'), -15.0, 'Produção rejeitada: falha na soldagem - Ordem rejeitada', NOW() - INTERVAL '3 days', NOW() - INTERVAL '3 days', 'WASTE');

-- ============================================
-- Seed completed
-- ============================================
