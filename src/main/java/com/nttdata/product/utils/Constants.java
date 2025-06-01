package com.nttdata.product.utils;

public class Constants {
    //Mensajes de respuesta
    public static final String SUCCESS_FIND_LIST_PRODUCT = "Lista de productos obtenida correctamente";
    public static final String SUCCESS_CREATE_PRODUCT = "Producto registrado correctamente";
    public static final String SUCCESS_FIND_PRODUCT = "Producto encontrado correctamente";
    public static final String SUCCESS_DELETE_PRODUCT = "Producto eliminado correctamente";
    public static final String SUCCESS_UPDATE_PRODUCT = "Producto actualizdo correctamente";
    public static final String ERROR_FIND_TRANSACTION = "No se encontró la transacción solicitada.";
    public static final String ERROR_FIND_PRODUCT = "No se encontró el producto solicitado.";
    public static final String ERROR_FIND_CUSTOMER = "No se encontró el cliente solicitado.";
    public static final String ERROR_INTERNAL  = "Hubo un problema con la solicitud";
    public static final String ERROR_VALIDATION_MESSAGE = "Error de validación: %s";

    // Constantes para validaciones de reglas de productos
    public static final String ERROR_BUSINESS_CANNOT_HAVE_PASSIVE_ACCOUNTS =
            "Los clientes comerciales no pueden tener cuentas de ahorro o de plazo fijo.";

    public static final String ERROR_PERSONAL_ONE_CREDIT_ONLY =
            "Los clientes personales sólo pueden tener un producto de crédito personal.";

    public static final String ERROR_BUSINESS_CURRENT_ACCOUNT_REQUIRES_HOLDER =
            "Las cuentas corrientes comerciales deben tener al menos un titular.";

    public static final String ERROR_PERSONAL_UNIQUE_PASSIVE_ACCOUNT =
            "Los clientes personales solo pueden tener una cuenta de tipo %s.";

    public static final String ERROR_SAVINGS_NO_MAINTENANCE_FEE =
            "Las cuentas de ahorro no deben tener comisión de mantenimiento.";
    public static final String ERROR_SAVINGS_REQUIRE_MONTHLY_LIMIT =
            "Las cuentas de ahorro deben tener un límite mensual de movimientos mayor a 0.";
    public static final String ERROR_CURRENT_ACCOUNT_REQUIRES_FEE =
            "Las cuentas corrientes deben tener comisión de mantenimiento mayor a 0.";
    public static final String ERROR_CURRENT_ACCOUNT_NO_MONTHLY_LIMIT =
            "Las cuentas corrientes no deben tener límite de movimientos mensuales.";
    public static final String ERROR_FIXED_TERM_NO_MAINTENANCE_FEE =
            "Las cuentas a plazo fijo no deben tener comisión de mantenimiento.";
    public static final String ERROR_FIXED_TERM_REQUIRE_TRANSACTION_DAY =
            "Las cuentas a plazo fijo deben tener definido un día de transacción entre 1 y 31.";
    public static final String ERROR_FIXED_TERM_REQUIRE_MONTHLY_LIMIT =
            "Las cuentas a plazo fijo deben tener un límite mensual de movimientos igual a 1.";
}
