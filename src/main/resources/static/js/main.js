document.addEventListener("DOMContentLoaded", () => {
    // Dynamic Booking Calculator (Details Page)
    setupBookingCalculator();

    // Auto-dismiss Flash Messages
    setupToastManager();
});

/**
 * Calculates and updates the total price dynamically when quantity changes.
 */
function setupBookingCalculator() {
    const qtyInput = document.getElementById("booking-quantity");
    const totalValueSpan = document.getElementById("booking-total-value");
    const ticketPriceInput = document.getElementById("ticket-price");

    if (qtyInput && totalValueSpan && ticketPriceInput) {
        const unitPrice = parseFloat(ticketPriceInput.value);
        
        const updatePrice = () => {
            const qty = parseInt(qtyInput.value) || 1;
            const total = qty * unitPrice;
            totalValueSpan.textContent = `$${total.toLocaleString()}`;
        };

        qtyInput.addEventListener("input", updatePrice);
        qtyInput.addEventListener("change", updatePrice);
        
        // Initial run
        updatePrice();
    }
}

/**
 * Handles showing and dismissing flash messages using the toast styling.
 */
function setupToastManager() {
    const toasts = document.querySelectorAll(".toast");
    toasts.forEach(toast => {
        // Auto dismiss after 4 seconds
        setTimeout(() => {
            toast.style.transition = "all 0.5s ease-out";
            toast.style.opacity = "0";
            toast.style.transform = "translateY(-40px)";
            
            setTimeout(() => {
                toast.remove();
            }, 500);
        }, 4000);

        // Click to close
        toast.addEventListener("click", () => {
            toast.remove();
        });
    });
}
