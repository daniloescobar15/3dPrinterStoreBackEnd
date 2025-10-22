# Guía de Migración a Arquitectura Hexagonal

## Estado Actual (en progreso)

Se ha refactorizado el proyecto a **Arquitectura Hexagonal**. Ahora existen dos versiones de algunos componentes:

### ✅ Nueva Estructura (Hexagonal)
```
✓ domain/payment/              - Entidades y puertos de dominio
✓ domain/authentication/       - Puertos de autenticación
✓ application/payment/         - Use cases
✓ application/authentication/  - Use cases
✓ infrastructure/adapter/      - Adaptadores entrada/salida
✓ infrastructure/persistence/  - Adaptadores de BD
✓ infrastructure/external/     - Adaptadores de APIs externas
✓ infrastructure/config/       - Configuración de arquitectura
```

### ⚠️ Estructura Antigua (deprecada)
```
✗ persistence/entity/Payment.java           → Reemplazada por domain/payment/entity/Payment.java
✗ persistence/repository/PaymentRepository   → Reemplazada por domain/payment/repository/PaymentRepository.java
✗ core/service/PuntoRedPaymentService.java   → Reemplazada por application/payment/usecase/
✗ infrastructure/entrypoint/controller/      → Reemplazada por infrastructure/adapter/in/web/
```

## Pasos para Completar la Migración

### 1. **Actualizar Imports en la Aplicación**

Cambiar en todos los archivos que aún usen las clases antiguas:

**De:**
```java
import com.printerstore.backend.persistence.entity.Payment;
import com.printerstore.backend.core.service.PuntoRedPaymentService;
```

**A:**
```java
import com.printerstore.backend.domain.payment.entity.Payment;
import com.printerstore.backend.application.payment.port.PaymentServicePort;
```

### 2. **Remapear Controladores (SI AÚN EXISTEN)**

Si tienes controladores antiguos usando `PuntoRedPaymentService`, cambiar a usar `PaymentServicePort`:

**De:**
```java
@Autowired
private PuntoRedPaymentService paymentService;

public void processPayment(...) {
    paymentService.processPayment(request, userId);
}
```

**A:**
```java
@Autowired
private PaymentServicePort paymentService;

public void processPayment(...) {
    paymentService.processPayment(request, userId);
}
```

### 3. **Eliminar Archivos Antiguos** (Cuando estés seguro)

Una vez que hayas verificado que todo funciona:

```bash
# Eliminar estructura antigua
rm -rf src/main/java/com/printerstore/backend/persistence/
rm -rf src/main/java/com/printerstore/backend/core/
# NO eliminar src/main/java/com/printerstore/backend/infrastructure/entrypoint/ 
# si tienes controllers que aún los usan
```

### 4. **Verificar Compilación**

```bash
mvn clean compile
```

### 5. **Ejecutar Pruebas**

```bash
mvn test
```

### 6. **Iniciar la Aplicación**

```bash
mvn spring-boot:run
```

## Verificación de Endpoints

Los endpoints siguen siendo iguales:

- `POST /payment/process` - Procesar pago
- `GET /payment/list` - Listar pagos del usuario
- `POST /payment/cancel` - Cancelar pago
- `POST /payment/callback` - Callback de Punto Red

## Cambios de Implementación

### Antes (Monolítico):
```java
@Service
public class PuntoRedPaymentService {
    public PaymentResponse processPayment(...) { ... }
    public List<Payment> getUserPayments(...) { ... }
    public void updatePaymentCallback(...) { ... }
    public CancelPaymentResponse cancelPayment(...) { ... }
}
```

### Después (Hexagonal):
```java
// PaymentServiceAdapter (Inversion Control)
@Service
public class PaymentServiceAdapter implements PaymentServicePort {
    private final ProcessPaymentUseCase processPaymentUseCase;
    private final GetUserPaymentsUseCase getUserPaymentsUseCase;
    private final UpdatePaymentCallbackUseCase updatePaymentCallbackUseCase;
    private final CancelPaymentUseCase cancelPaymentUseCase;
    
    public ProcessPaymentResponse processPayment(...) {
        return processPaymentUseCase.execute(...);
    }
}

// ProcessPaymentUseCase (Responsabilidad única)
@Service
public class ProcessPaymentUseCase {
    public ProcessPaymentResponse execute(...) { ... }
}
```

## Beneficios Inmediatos

✅ **Testabilidad**: Más fácil testear sin Spring
✅ **Mantenibilidad**: Cada clase tiene una responsabilidad
✅ **Flexibilidad**: Cambiar de BD o API externa es fácil
✅ **Independencia**: El dominio no depende de frameworks

## Próximos Pasos Recomendados

1. **Tests Unitarios**
   - Crear tests para cada Use Case
   - Mock de gateways y repositorios

2. **Tests de Integración**
   - Probar adaptadores con BD real
   - Probar adaptadores con APIs externas

3. **Mejorar Manejo de Errores**
   - Crear excepciones de negocio personalizadas
   - Mapear errores de APIs externas

4. **Documentación de API**
   - Actualizar Swagger/OpenAPI
   - Documentar DTOs de entrada/salida

## Dudas o Problemas

Si encuentras errores de compilación:

1. Verifica que los imports estén correctos
2. Revisa que las inyecciones de dependencia sean válidas
3. Asegúrate que `@Component`, `@Service`, `@Repository` estén en los adaptadores
4. Verifica que `PaymentJpaRepository` esté escaneado por Spring

## Monitoreo

Después de la migración, verifica:

- ✓ La aplicación inicia sin errores
- ✓ Los endpoints responden correctamente
- ✓ La base de datos se actualiza
- ✓ Las integraciones externas funcionan
- ✓ Los logs son informativos

¡La migración a Arquitectura Hexagonal está en progreso!