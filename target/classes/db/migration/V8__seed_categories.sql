INSERT INTO categories (id, type, slug_tr, slug_en, title_tr, title_en, description_tr, description_en, icon, sort_order)
VALUES
  ('frontend',    'POSITION',  'frontend-gelistirici',  'frontend-developer',
   'Frontend Developer',   'Frontend Developer',
   'React, JavaScript, CSS ve modern web geliştirme konularında bilginizi test edin.',
   'Test your knowledge in React, JavaScript, CSS and modern web development.',
   'Monitor', 1),

  ('backend',     'POSITION',  'backend-gelistirici',   'backend-developer',
   'Backend Developer',    'Backend Developer',
   'Java, Spring Boot, REST API, veritabanı tasarımı ve dağıtık sistemler.',
   'Java, Spring Boot, REST APIs, database design and distributed systems.',
   'Server', 2),

  ('fullstack',   'POSITION',  'fullstack-gelistirici', 'fullstack-developer',
   'Fullstack Developer',  'Fullstack Developer',
   'Hem frontend hem backend teknolojilerini kapsayan sorular.',
   'Questions covering both frontend and backend technologies.',
   'Layers', 3),

  ('mobile',      'POSITION',  'mobil-gelistirici',     'mobile-developer',
   'Mobil Gelistirici',    'Mobile Developer',
   'React Native, Flutter ve mobil uygulama geliştirme.',
   'React Native, Flutter and mobile application development.',
   'Smartphone', 4),

  ('devops',      'POSITION',  'devops-muhendisi',      'devops-engineer',
   'DevOps Muhendisi',     'DevOps Engineer',
   'CI/CD, Docker, Kubernetes, bulut altyapısı ve otomasyon.',
   'CI/CD, Docker, Kubernetes, cloud infrastructure and automation.',
   'GitBranch', 5),

  ('algorithms',  'TECHNICAL', 'algoritmalar',          'algorithms',
   'Algoritmalar & Veri Yapilari', 'Algorithms & Data Structures',
   'Sıralama, arama, graflar, ağaçlar ve karmaşıklık analizi.',
   'Sorting, searching, graphs, trees and complexity analysis.',
   'Cpu', 6),

  ('databases',   'TECHNICAL', 'veritabanlari',         'databases',
   'Veritabanlari',        'Databases',
   'SQL, NoSQL, indeksleme, transaction ve sorgu optimizasyonu.',
   'SQL, NoSQL, indexing, transactions and query optimization.',
   'Database', 7),

  ('system-design', 'TECHNICAL', 'sistem-tasarimi',     'system-design',
   'Sistem Tasarimi',      'System Design',
   'Ölçeklenebilir ve dayanıklı sistemlerin tasarım ilkeleri.',
   'Design principles for scalable and resilient systems.',
   'Network', 8),

  ('javascript',  'LANGUAGE',  'javascript',            'javascript',
   'JavaScript',           'JavaScript',
   'ES6+, asenkron programlama, prototip zinciri ve tarayıcı API''leri.',
   'ES6+, async programming, prototype chain and browser APIs.',
   'Code2', 9),

  ('typescript',  'LANGUAGE',  'typescript',            'typescript',
   'TypeScript',           'TypeScript',
   'Tip sistemi, generics, decorator ve TypeScript en iyi pratikleri.',
   'Type system, generics, decorators and TypeScript best practices.',
   'FileCode', 10),

  ('java',        'LANGUAGE',  'java',                  'java',
   'Java',                 'Java',
   'OOP, koleksiyonlar, Java Streams, eşzamanlılık ve JVM.',
   'OOP, collections, Java Streams, concurrency and JVM.',
   'Coffee', 11),

  ('python',      'LANGUAGE',  'python',                'python',
   'Python',               'Python',
   'Python sözdizimi, veri yapıları, dekoratörler ve standart kütüphane.',
   'Python syntax, data structures, decorators and standard library.',
   'Terminal', 12);
