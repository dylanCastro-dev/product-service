package com.nttdata.product.utils;

public class Constants {
    //Mensajes de respuesta
    public static final String SUCCESS_FIND_LIST_PRODUCT = "Lista de productos obtenida correctamente";
    public static final String SUCCESS_CREATE_PRODUCT = "Producto registrado correctamente";
    public static final String SUCCESS_FIND_PRODUCT = "Producto encontrado correctamente";
    public static final String SUCCESS_DELETE_PRODUCT = "Producto eliminado correctamente";
    public static final String SUCCESS_UPDATE_PRODUCT = "Producto actualizdo correctamente";
    public static final String ERROR_FIND_PRODUCT  = "Producto no encontrado";
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
}
