# UC01 — Generate Population Reports
**Actor:** Viewer  
**Pre:** Database available  
**Main flow:** Select report type (R01–R32) → system queries DB → returns sorted results.  
**Alt flows:** DB unreachable → error logged; invalid N → validation message.  
**Post:** Results rendered / exported.
