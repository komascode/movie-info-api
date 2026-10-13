const nowPlayingMovies = [
  { title: "미션 임파서블: 파이널 레코닝", releaseDate: "2025-05-22", rating: "12세" },
  { title: "인사이드 아웃 2", releaseDate: "2024-06-12", rating: "전체" },
  { title: "범죄도시 4", releaseDate: "2024-04-24", rating: "15세" },
  { title: "듄: 파트2", releaseDate: "2024-02-28", rating: "12세" },
  { title: "파묘", releaseDate: "2024-02-22", rating: "15세" }
];

const movieList = document.getElementById("movie-list");

movieList.innerHTML = nowPlayingMovies
  .map(
    (movie) => `
      <li class="movie-item">
        <div class="movie-title">${movie.title}</div>
        <div class="movie-meta">개봉일: ${movie.releaseDate} · 관람등급: ${movie.rating}</div>
      </li>
    `
  )
  .join("");
