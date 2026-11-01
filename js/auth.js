const users=()=>JSON.parse(localStorage.getItem("users")||"[]");
if (document.getElementById("registerForm")) {
    registerForm.onsubmit = async (e) => {
        e.preventDefault();

        const nameValue = document.getElementById("name").value;
        const emailValue = document.getElementById("email").value;
        const passwordValue = document.getElementById("password").value;

        try {
            const response = await fetch("http://localhost:8080/register", {
                method: "POST",
                headers: {
                    "Content-Type": "application/x-www-form-urlencoded"
                },
                body:
                    "name=" + encodeURIComponent(nameValue) +
                    "&email=" + encodeURIComponent(emailValue) +
                    "&password=" + encodeURIComponent(passwordValue)
            });

            const result = await response.text();

            if (result === "SUCCESS") {
                alert("Registration successful!");
                location.href = "login.html";
            } else if (result === "EXISTS") {
                alert("Email already registered.");
            } else {
                alert("Registration failed.");
            }

        } catch (error) {
            alert("Backend server is not running.");
            console.error(error);
        }
    };
}


if (document.getElementById("loginForm")) {
    loginForm.onsubmit = async (e) => {
        e.preventDefault();

        const emailValue = document.getElementById("email").value;
        const passwordValue = document.getElementById("password").value;

        try {
            const response = await fetch("http://localhost:8080/login", {
                method: "POST",
                headers: {
                    "Content-Type": "application/x-www-form-urlencoded"
                },
                body:
                    "email=" + encodeURIComponent(emailValue) +
                    "&password=" + encodeURIComponent(passwordValue)
            });

            const result = await response.text();

            if (result === "SUCCESS") {
                localStorage.setItem("currentUser", JSON.stringify({
                    email: emailValue
                }));

                alert("Login successful!");
                location.href = "index.html";
            } else {
                alert("Invalid email or password.");
            }

        } catch (error) {
            alert("Backend server is not running.");
            console.error(error);
        }
    };
}


/* LOGOUT */

if (document.getElementById("logout")) {

    const logged = localStorage.getItem("currentUser");

    if (logged) {

        document.getElementById("logout").classList.remove("hide");

        const navLogin = document.getElementById("navLogin");

        if (navLogin) {
            navLogin.classList.add("hide");
        }

        document.getElementById("logout").onclick = (e) => {

            e.preventDefault();

            localStorage.removeItem("currentUser");

            location.reload();
        };
    }
}