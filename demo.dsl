workspace {
  model {
    user = person "User" "A user"

    search = softwareSystem "Product Search" "Search capabilities offered over GraphQL" "Existing System"

    priceRunner = softwareSystem "PriceRunner" "Compare prices from multiple suppliers" "External"

    system = softwareSystem "Price Tracker" "A system to track prices on products." {
      web = container "Web App" "A web app to track prices" "React"
      app = container "Mobile App" "An Android app to track prices" "React Native"
      db = container "Database" "User profile data" "CouchDB" "Database"
      bff = container "App Backend" "Backend for the app" "Node JS"
    }

    user -> app "Uses"
    user -> web "Uses"
    app -> bff "Uses" "HTTP, gRPC"
    web -> bff "Uses" "HTTP, gRPC"
    bff -> db "Read/Write" "TCP"
    bff -> search "Search" "HTTP, GraphQL"
    bff -> priceRunner "Search" "HTTP"
  }

  views {
    systemContext system {
      include *
    }

    container system {
      include *
    }
  }
}
