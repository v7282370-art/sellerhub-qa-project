# XPath и CSS селекторы — практика для интервью

## Структура тестового экрана (HTML)
```html
<div id="campaigns-app">
  <header class="app-header">
    <button id="menu-btn" class="icon-btn">☰</button>
    <h1>SellerHub Campaigns</h1>
  </header>
  
  <div class="campaign-list" data-testid="campaign-list">
    <div class="campaign-item" data-id="101">
      <h3>Black Friday Sale</h3>
      <span class="status active">Active</span>
      <button class="edit-btn">Edit</button>
    </div>
    <div class="campaign-item" data-id="102">
      <h3>Winter Promo</h3>
      <span class="status paused">Paused</span>
      <button class="edit-btn">Edit</button>
    </div>
  </div>
  
  <footer>
    <button type="submit" class="btn-primary">Save Campaign</button>
  </footer>
</div>